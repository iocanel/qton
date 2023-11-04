# Qton - Quick Tekton

A command line tool that helps users quickly try out their tekton tasks as if they were local commands.

## How it works

The tool is looking for task yaml files under `.qton/taks` or `$HOME/.qton/tasks`.
It allows the user to `Quickly` execute any of these Tasks by creating a TaskRun
that follows cretain conventions for handling workspaces and parameters.

Once the TaskRun is created it logs the output, as if the task was running locally.

So, there is no magic here! Just some conventions to speed up your workflow and make 
Tekton feel like being a local thing.

### Example

![qton demo](qton-demo.gif)


### Conventions

#### Workspaces

For each workspace referenced in a `Task` qton looks up the cluster to find a matching resource. Matching resources are used to generate `WorkspaceBindings` that are added the final `TaskRun`.
Matching resources can be:

- [PersistentVolumeClaims](#persistentvolumeclaims)
- [ConfigMaps](#configmaps)
- [Secrets](#secrets)

##### PersistentVolumeClaims

For each workspace referenced in a `Task` with a `-pvc` or `-dir` suffix, that has a matching `PersistentVolumeClaim` installed in the cluster, a `WorkspaceBinding` for it is added to the `TaskRun`.
Matching is performed with or without the suffixes.

##### ConfigMaps

For each workspace referenced in a `Task` with a `-configmap`, `-cm` or `-cfg` suffix, that has a matching `ConfigMap` installed in the cluster, a `WorkspaceBinding` for it is added to the `TaskRun`.
Matching is performed with or without the suffixes.

##### Secrets

For each workspace referenced in a `Task` with a `-secret` or `-scfg` suffix, that has a matching `Secret` installed in the cluster, a `WorkspaceBinding` for it is added to the `TaskRun`.
Matching is performed with or without the suffixes.
