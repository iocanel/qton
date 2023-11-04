package io.qton.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.qton.TaskCatalog;
import io.qton.TaskCatalogService;
import picocli.CommandLine.Option;

public class TaskCatalogOptions {

  private static final TaskCatalogService catalogService = new TaskCatalogService();

  @Option(names = { "--user" }, defaultValue = "", paramLabel = "USER", order = 1, description = "Use the user catalog.")
  boolean user;

  @Option(names = { "--user-dir" }, paramLabel = "USER_DIR", order = 2, description = "Use the user catalog directory.")
  Optional<Path> userDirectory = Optional.empty();

  public TaskCatalog getCatalog() {
    return catalogService.readCatalog(user ? userDirectory.orElse(Paths.get(System.getProperty("user.home")))
        : Paths.get(System.getProperty("user.dir")));

  }

}
