# HyHardwarePatch
This early plugin provides a custom method for getting the UUID from the system, which is necessary in isolated contexts such as Docker containers.

> Since version 1.0.1, this plugin no longer requires the Hyxin plugin to work.

## Transformers

### HytaleServerTransformer

Modifies the server's scheduled executor to use multiple threads instead of a single thread. The thread count is set to
half of the available processor cores (minimum 2 threads).

### ChunkGeneratorTransformer

Adjusts the chunk generation thread pool size from 75% to 25% of available processor cores to optimize server
performance.

### To install the plugin, create a directory in the root directory `earlyplugins` and put the .jar inside + when you launch the server, use this parameter `--accept-early-plugins`.