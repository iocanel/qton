package io.qton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.quarkus.cli.plugin.Catalog;

public class TaskCatalog implements Catalog<TaskCatalog> {

  private final List<Task> tasks = new ArrayList<>();

  private static final YAMLMapper OBJECT_MAPPER = new YAMLMapper();

  @JsonIgnore
  private final Optional<Path> catalogLocation;

  public TaskCatalog(Optional<Path> catalogLocation) {
    this.catalogLocation = catalogLocation;
    catalogLocation.ifPresent(path -> {
      try (Stream<Path> stream = streamYamlFiles(path)) {
        stream.forEach(p -> {
          try {
            Task task = OBJECT_MAPPER.readValue(p.toFile(), Task.class);
            if (task.getKind().equals("Task")) {
              tasks.add(task);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
      }
    });
  }

  public List<Task> getTasks() {
    return tasks;
  }

  @Override
  public Optional<Path> getCatalogLocation() {
    return catalogLocation;
  }

  @Override
  public TaskCatalog withCatalogLocation(Optional<Path> catalogLocation) {
    return new TaskCatalog(catalogLocation);
  }

  @Override
  public TaskCatalog refreshLastUpdate() {
    return this;
  }

  public static Stream<Path> streamYamlFiles(Path directory) {
    if (Files.isDirectory(directory)) {
      try {
        return Files.walk(directory)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".yaml") || path.toString().endsWith(".yml"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return List.<Path> of().stream();
  }

}
