package imageboard.app;

import imageboard.api.ImageboardClient;
import imageboard.api.ImageboardClients;
import imageboard.api.ImageboardProvider;
import imageboard.errors.ImageboardException;
import imageboard.query.SearchQuery;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int status = run(args, System.out, System.err, config ->
                ImageboardClients.builder(config.provider()).build());
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err, Function<Config, ImageboardClient> clientFactory) {
        Config config = Config.parse(args);
        if (config.help()) {
            printUsage(out);
            return 0;
        }
        if (config.error() != null) {
            err.println(config.error());
            printUsage(err);
            return 2;
        }

        try {
            ImageboardClient client = clientFactory.apply(config);
            var query = SearchQuery.builder()
                    .tags(config.tags())
                    .limit(config.limit())
                    .page(1)
                    .build();
            var result = client.searchPosts(query);
            out.println("provider=" + config.provider().name().toLowerCase(Locale.ROOT));
            out.println("count=" + result.items().size());
            result.items().stream().limit(5).forEach(post -> out.println(post.id() + "\t" + post.fileUrl()));
            return 0;
        } catch (ImageboardException ex) {
            err.println("Imageboard request failed: " + ex.getMessage());
            return 1;
        }
    }

    private static void printUsage(PrintStream stream) {
        stream.println("Usage: Main [provider] [tag1 tag2 ...] [--limit=N]");
        stream.println("  provider: danbooru | gelbooru | rule34 (default: danbooru)");
    }

    record Config(ImageboardProvider provider, java.util.List<String> tags, int limit, boolean help, String error) {
        static Config parse(String[] args) {
            if (args == null || args.length == 0) {
                return new Config(ImageboardProvider.DANBOORU, java.util.List.of("rating:safe"), 20, true, null);
            }
            if ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0])) {
                return new Config(ImageboardProvider.DANBOORU, java.util.List.of("rating:safe"), 20, true, null);
            }

            ImageboardProvider provider;
            try {
                provider = ImageboardProvider.valueOf(args[0].trim().toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                return new Config(ImageboardProvider.DANBOORU, java.util.List.of(), 20, false,
                        "Unknown provider: " + args[0]);
            }

            int limit = 20;
            var tags = new java.util.ArrayList<String>();
            for (String arg : Arrays.copyOfRange(args, 1, args.length)) {
                if (arg.startsWith("--limit=")) {
                    String raw = arg.substring("--limit=".length());
                    try {
                        limit = Math.max(1, Math.min(100, Integer.parseInt(raw)));
                    } catch (NumberFormatException e) {
                        return new Config(provider, java.util.List.of(), 20, false, "Invalid --limit value: " + raw);
                    }
                } else if (!arg.isBlank()) {
                    tags.add(arg);
                }
            }
            if (tags.isEmpty()) {
                tags.add("rating:safe");
            }
            return new Config(provider, java.util.List.copyOf(tags), limit, false, null);
        }
    }
}
