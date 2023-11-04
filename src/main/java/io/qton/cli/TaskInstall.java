package io.qton.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.qton.TaskCatalog;
import io.qton.TaskCatalogService;
import io.qton.cli.common.OutputOptionMixin;
import io.qton.cli.utils.Clients;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "install", header = "Install qton tasks. ")
public class TaskInstall implements Callable<Integer> {

  private final TaskCatalogService catalogService = new TaskCatalogService();

  @Mixin(name = "output")
  OutputOptionMixin output;

  @Option(names = {
      "--user" }, defaultValue = "", paramLabel = "USER", order = 4, description = "Use the user catalog.")
  boolean user;

  @Option(names = {
      "--user-dir" }, paramLabel = "USER_DIR", order = 5, description = "Use the user catalog directory.")
  Optional<Path> userDirectory = Optional.empty();

  @Option(names = {
      "--all" }, defaultValue = "", paramLabel = "all", order = 6, description = "Insall all plugins in the catalog.")
  boolean all;

  @Parameters(index = "0", arity = "0..1", paramLabel = "TASK", description = "Task name.")
  String taskName;

  @Override
  public Integer call() throws Exception {
    TaskCatalog catalog = catalogService.readCatalog(user
        ? userDirectory.orElse(Paths.get(System.getProperty("user.home")))
        : Paths.get(System.getProperty("user.home")));

    List<String> taskNames = all
        ? catalog.getTasks().stream().map(t -> t.getMetadata().getName()).collect(java.util.stream.Collectors.toList())
        : List.of(taskName);

    for (String name : taskNames) {
      Optional<Task> task = catalog.getTasks().stream().filter(t -> t.getMetadata().getName().equals(name)).findFirst();
      if (task.isPresent()) {
        Task t = task.get();
        Clients.kubernetes().resource(t).serverSideApply();
        output.info("Task %s (re) installed.", t.getMetadata().getName());
      } else {
        output.error("Task %s not found.", taskName);
        return ExitCode.SOFTWARE;
      }
    }
    return ExitCode.OK;
  }

  private final boolean isInstalled(String taskName) {
    return Clients.tekton().v1beta1().tasks().withName(taskName).get() != null ||
        Clients.tekton().v1().tasks().withName(taskName).get() != null;
  }
}
