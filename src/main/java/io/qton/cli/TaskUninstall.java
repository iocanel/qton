package io.qton.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Callable;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.tekton.client.DefaultTektonClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.qton.TaskCatalog;
import io.qton.TaskCatalogService;
import io.qton.cli.common.OutputOptionMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "uninstall", header = "Uninstall qton tasks. ")
public class TaskUninstall implements Callable<Integer> {

  private final TaskCatalogService catalogService = new TaskCatalogService();
  private final TektonClient tektonClient = new DefaultTektonClient();
  private final KubernetesClient kubernetesClient = new KubernetesClientBuilder().build();

  @Mixin(name = "output")
  OutputOptionMixin output;

  @Option(names = {
      "--user" }, defaultValue = "", paramLabel = "USER", order = 4, description = "Use the user catalog.")
  boolean user;

  @Option(names = {
      "--user-dir" }, paramLabel = "USER_DIR", order = 5, description = "Use the user catalog directory.")
  Optional<Path> userDirectory = Optional.empty();

  @Parameters(index = "0", paramLabel = "TASK", description = "Task name.")
  String taskName;

  @Override
  public Integer call() throws Exception {
    TaskCatalog catalog = catalogService.readCatalog(user
        ? userDirectory.orElse(Paths.get(System.getProperty("user.home")))
        : Paths.get(System.getProperty("user.home")));

    if (!isInstalled(taskName)) {
      output.info("Task %s is already not.", taskName);
      return ExitCode.USAGE;
    }
    Optional<Task> task = catalog.getTasks().stream().filter(t -> t.getMetadata().getName().equals(taskName)).findFirst();
    if (task.isPresent()) {
      Task t = task.get();
      kubernetesClient.resource(t).delete();
      output.info("Task %s uninstalled.", taskName);
      return ExitCode.OK;
    }
    output.error("Task %s not install.", taskName);
    return ExitCode.USAGE;
  }

  private final boolean isInstalled(String taskName) {
    return tektonClient.v1beta1().tasks().withName(taskName).get() != null ||
        tektonClient.v1().tasks().withName(taskName).get() != null;
  }
}
