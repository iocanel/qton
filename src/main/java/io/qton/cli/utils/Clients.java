package io.qton.cli.utils;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.tekton.client.DefaultTektonClient;
import io.fabric8.tekton.client.TektonClient;

public class Clients {

  private static final KubernetesClient KUBERNETES = new KubernetesClientBuilder().build();
  private static final TektonClient TEKTON = new DefaultTektonClient();

  public static KubernetesClient kubernetes() {
    return KUBERNETES;
  }

  public static TektonClient tekton() {
    return TEKTON;
  }
}
