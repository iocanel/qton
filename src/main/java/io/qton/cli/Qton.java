package io.qton.cli;

import java.util.concurrent.Callable;

import io.qton.cli.common.OutputOptionMixin;
import io.quarkus.runtime.QuarkusApplication;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(name = "qton", subcommands = { TaskList.class, TaskInstall.class, TaskUninstall.class, TaskExec.class,
    Completion.class }, scope = ScopeType.INHERIT, sortOptions = false, showDefaultValues = true, versionProvider = Version.class, subcommandsRepeatable = false, mixinStandardHelpOptions = false, commandListHeading = "%nCommands:%n", synopsisHeading = "%nUsage: ", optionListHeading = "Options:%n", headerHeading = "%n", parameterListHeading = "%n")
public class Qton implements QuarkusApplication, Callable<Integer> {

  static {
    System.setProperty("picocli.endofoptions.description", "End of command line options.");
  }

  @Inject
  CommandLine.IFactory factory;

  @CommandLine.Mixin(name = "output")
  OutputOptionMixin output;

  @CommandLine.Spec
  protected CommandLine.Model.CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    output.info("%n@|bold Qton CLI|@ version %s", Version.clientVersion());
    output.info("");
    output.info("Quick Tekton task runner.");
    spec.commandLine().usage(output.out());

    output.info("");
    output.info("Use \"qton <command> --help\" for more information about a given command.");

    return spec.exitCodeOnUsageHelp();
  }

  @Override

  public int run(String... args) throws Exception {
    CommandLine cmd = factory == null ? new CommandLine(this) : new CommandLine(this, factory);
    cmd.setUnmatchedArgumentsAllowed(true);
    cmd.setUnmatchedOptionsArePositionalParams(true);
    return cmd.execute(args);
  }

  public OutputOptionMixin getOutput() {
    return output;
  }

  public CommandLine.Model.CommandSpec getSpec() {
    return spec;
  }

}
