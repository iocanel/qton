package io.qton.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.pipeline.v1beta1.TaskRunBuilder;
import io.fabric8.tekton.pipeline.v1beta1.WorkspaceBinding;
import io.qton.TaskCatalog;
import io.qton.cli.common.OutputOptionMixin;
import io.qton.cli.utils.Clients;
import io.qton.cli.utils.TaskRuns;
import io.qton.cli.utils.WorkspaceBindings;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "exec", header = "Execute qton tasks.")
public class TaskExec implements Callable<Integer> {

  @Mixin(name = "output")
  OutputOptionMixin output;

  @ArgGroup(order = 1, heading = "%nCatalog:%n")
  TaskCatalogOptions catalogOptions = new TaskCatalogOptions();

  @Parameters(index = "0", paramLabel = "TASK", description = "Task name.")
  String taskName;

  @Parameters(description = "Additional parameters passed to the build system")
  private List<String> taskArgs = new ArrayList<>();

  @Override
  public Integer call() throws Exception {
    //    output.info("Additional params:" + params.stream().collect(Collectors.joining(", ")));
    TaskCatalog catalog = catalogOptions.getCatalog();

    Optional<Task> task = catalog.getTasks().stream().filter(t -> t.getMetadata().getName().equals(taskName)).findFirst();
    if (task.isPresent()) {
      Task t = task.get();

      if (Clients.kubernetes().resource(t).get() != null) {
        Clients.kubernetes().resource(t).delete();
      }
      Clients.kubernetes().resource(t).serverSideApply();

      Map<String, PersistentVolumeClaim> pvcClaims = Clients.kubernetes().persistentVolumeClaims().list().getItems().stream()
          .collect(Collectors.toMap(p -> p.getMetadata().getName(), Function.identity()));
      Map<String, ConfigMap> configMaps = Clients.kubernetes().configMaps().list().getItems().stream()
          .collect(Collectors.toMap(c -> c.getMetadata().getName(), Function.identity()));
      Map<String, Secret> secrets = Clients.kubernetes().secrets().list().getItems().stream()
          .collect(Collectors.toMap(s -> s.getMetadata().getName(), Function.identity()));

      List<WorkspaceBinding> workspaceBindings = new ArrayList<>();

      t.getSpec().getWorkspaces().forEach(w -> {
        String name = w.getName();
        WorkspaceBindings.forName(name).ifPresent(workspaceBindings::add);
      });

      String taskRunName = taskName + "-run";
      TaskRun taskRun = new TaskRunBuilder()
          .withNewMetadata()
          .withName(taskRunName)
          .endMetadata()
          .withNewSpec()
          .withNewTaskRef()
          .withName(t.getMetadata().getName())
          .endTaskRef()
          .withWorkspaces(workspaceBindings)
          .addNewParam()
          .withName("ARGS")
          .withNewValue()
          .withArrayVal(taskArgs.stream().skip(1).toList())
          .endValue()
          .endParam()
          .endSpec()
          .build();

      if (Clients.kubernetes().resource(taskRun).get() != null) {
        Clients.kubernetes().resource(taskRun).delete();
      }
      Clients.kubernetes().resource(taskRun).serverSideApply();
      output.debug("Created TaskRun %s.", taskRunName);
      TaskRuns.waitUntilReady(taskRunName);
      TaskRuns.log(taskRunName);
      return ExitCode.OK;
    }
    output.error("Task %s not found.", taskName);
    return ExitCode.USAGE;
  }

}
