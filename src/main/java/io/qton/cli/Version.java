package io.qton.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Callable;

import io.qton.cli.common.HelpOption;
import io.qton.cli.common.OutputOptionMixin;
import io.qton.cli.common.PropertiesOptions;
import io.smallrye.common.classloader.ClassPathUtils;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@CommandLine.Command(name = "version", header = "Display CLI version information.", hidden = true)
public class Version implements CommandLine.IVersionProvider, Callable<Integer> {

  private static String version;

  @CommandLine.Mixin(name = "output")
  OutputOptionMixin output;

  @CommandLine.Mixin
  HelpOption helpOption;

  @CommandLine.ArgGroup(exclusive = false, validate = false)
  protected PropertiesOptions propertiesOptions = new PropertiesOptions();

  @CommandLine.Spec
  CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    // Gather/interpolate the usual version information via IVersionProvider handling
    output.printText(getVersion());
    return CommandLine.ExitCode.OK;
  }

  @Override
  public String[] getVersion() throws Exception {
    return new String[] { clientVersion() };
  }

  public static String clientVersion() {
    if (version != null) {
      return version;
    }

    final Properties props = new Properties();
    final URL qtonPropertiesUrl = Thread.currentThread().getContextClassLoader().getResource("qton.properties");
    if (qtonPropertiesUrl == null) {
      throw new RuntimeException("Failed to locate qton.properties on the classpath");
    }

    // we have a special case for file and jar as using getResourceAsStream() on Windows might cause file locks
    if ("file".equals(qtonPropertiesUrl.getProtocol()) || "jar".equals(qtonPropertiesUrl.getProtocol())) {
      ClassPathUtils.consumeAsPath(qtonPropertiesUrl, p -> {
        try (BufferedReader reader = Files.newBufferedReader(p)) {
          props.load(reader);
        } catch (IOException e) {
          throw new RuntimeException("Failed to load qton.properties", e);
        }
      });
    } else {
      try {
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qton.properties"));
      } catch (IOException e) {
        throw new IllegalStateException("Failed to load qton.properties", e);
      }
    }

    version = props.getProperty("qton-version");
    if (version == null) {
      throw new RuntimeException("Failed to locate qton-version property in the bundled qton.properties");
    }

    return version;
  }
}
