package io.qton.cli;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.qton.TaskCatalog;
import io.qton.cli.common.OutputOptionMixin;
import io.qton.cli.common.Table;
import io.qton.cli.utils.Clients;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;

@Command(name = "list", header = "List qton tasks. ")
public class TaskList implements Callable<Integer> {

  private final Set<String> INSTALLED_TASKS = new HashSet<>();

  @Mixin(name = "output")
  OutputOptionMixin output;

  @ArgGroup(order = 1, heading = "%nCatalog:%n")
  TaskCatalogOptions catalogOptions = new TaskCatalogOptions();

  @Override
  public Integer call() throws Exception {
    TaskCatalog catalog = catalogOptions.getCatalog();
    getInstalledTasks();

    List<String> headers = List.of("Name", "Installed", "Description");
    List<Function<Task, String>> mappers = List.of(t -> t.getMetadata().getName(),
        t -> isInstalled(t.getMetadata().getName()) ? "    *    " : "         ",
        t -> t.getSpec().getDescription());

    Table<Task> table = new Table(headers, mappers, catalog.getTasks());
    table.print();
    return ExitCode.OK;
  }

  private final void getInstalledTasks() {
    try {
      Clients.tekton().v1beta1().tasks().list().getItems().stream().map(Task::getMetadata).map(ObjectMeta::getName)
          .forEach(INSTALLED_TASKS::add);
    } catch (Exception e) {
      // ignore
    }
  }

  private final boolean isInstalled(String taskName) {
    return INSTALLED_TASKS.contains(taskName);
  }
}
