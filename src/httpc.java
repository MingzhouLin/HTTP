import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class httpc {

    public static CurlCommandLine parseCurlCommandLine(String cmd) {
        CurlCommandLine curlCommandLine = new CurlCommandLine();
        Request request = new Request();

        String[] cmdArray = cmd.split(" ");

        if (cmd.contains("help")) {
            curlCommandLine.setHelp(true);
            if (cmd.contains("get")) {
                System.out.println("usage: httpc get [-v] [-h key:value] URL\n" +
                        "Get executes a HTTP GET request for a given URL.\n" +
                        "-v           Prints the detail of the response such as protocol, status, and headers.\n" +
                        "-h key:value Associates headers to HTTP Request with the format 'key:value'.\n");
            } else if (cmd.contains("post")) {
                System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n" +
                        "Post executes a HTTP POST request for a given URL with inline data or from file.\n" +
                        "-v           Prints the detail of the response such as protocol, status, and headers.\n" +
                        "-h key:value Associates headers to HTTP Request with the format 'key:value'.\n" +
                        "-d string    Associates an inline data to the body HTTP POST request.\n" +
                        "-f file      Associates the content of a file to the body HTTP POST request.\n" +
                        "Either [-d] or [-f] can be used but not both.\n");
            } else {
                System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n" +
                        "Usage:\n" +
                        "    httpc command [arguments]\n" +
                        "The commands are:\n" +
                        "    get     executes a HTTP GET request and prints the response.\n" +
                        "    post    executes a HTTP POST request and prints the response.\n" +
                        "    help    prints this screen.\n" +
                        "Use \"httpc help [command]\" for more information about a command.");
            }
            return curlCommandLine;
        } else {
            curlCommandLine.setHelp(false);
        }

        if (cmd.contains("get")) {
            curlCommandLine.setRequestType("get");
        }

        if (cmd.contains("post")) {
            curlCommandLine.setRequestType("post");
        }

        if (cmd.contains("-v")) {
            curlCommandLine.setVerbose(true);
        } else {
            curlCommandLine.setVerbose(false);
        }

        if (cmd.contains("-h")) {
            curlCommandLine.setHaveHeaders(true);
            String headerString = findTarget("-h", cmdArray);
            String[] headerSplit = headerString.split(";");
            ArrayList<String> headers = new ArrayList<>();
            for (int i = 0; i < headerSplit.length; i++) {
                headers.add(headerSplit[i]);
            }
            curlCommandLine.setHeaders(headers);
        } else {
            curlCommandLine.setHaveHeaders(false);
        }

        if (cmd.contains("-d")) {
            curlCommandLine.setHaveInlineData(true);
            String inlineData = findContent(cmd, "-d ");
            curlCommandLine.setInlineData(inlineData);
        } else {
            curlCommandLine.setHaveInlineData(false);
        }

        if (cmd.contains("-f")) {
            curlCommandLine.setHaveFile(true);
            String file = findContent(cmd, "-f ");
            curlCommandLine.setFile(file);
        } else {
            curlCommandLine.setHaveFile(false);
        }

        if (cmd.contains("-o")) {
            curlCommandLine.setOutput(true);
            curlCommandLine.setOutputFile("./" + cmdArray[cmdArray.length - 1]);
            curlCommandLine.setUrl(cmdArray[cmdArray.length - 3]);
        } else {
            curlCommandLine.setOutput(false);
        }

        if (cmd.contains("-p")){
            int port=Integer.parseInt(findContent(cmd, "-p "));
            curlCommandLine.setPort(port);
        }

        if (!curlCommandLine.isOutput()) {
            curlCommandLine.setUrl(cmdArray[cmdArray.length - 1]);
        }

        curlCommandLine.setRequest(configRequest(curlCommandLine));

        return curlCommandLine;
    }

    private static String findContent(String cmd, String mark) {
        String[] splidByDashd = cmd.split(mark);
        String dataPart = splidByDashd[1];
        String[] splitBySingleQuote = dataPart.split("'");
        String data = splitBySingleQuote[1];
        return data;
    }

    private static Request configRequest(CurlCommandLine curlCommandLine) {
        Request request = new Request();

        String host = getHostFromUrl(curlCommandLine.getUrl());
        String path = getPathFromUrl(curlCommandLine.getUrl(), host);

        request.setPort(curlCommandLine.port);
        request.setHost(host);
        request.setPath(path);
        if (curlCommandLine.getHeaders() != null) {
            request.setRequestHeaders(curlCommandLine.getHeaders());
        } else {
            request.setRequestHeaders(new ArrayList<String>());
        }
        request.requestType = curlCommandLine.requestType;
        if (curlCommandLine.haveInlineData) {
            request.requestBody = formatRequestBody(curlCommandLine.inlineData);
        } else if (curlCommandLine.haveFile) {
            request.requestBody = readFile(curlCommandLine.file);
        } else {
            request.requestBody = "";
        }
        if (request.requestType.equals("post")) {
            request.requestHeaders.add("Content-Length:" + request.requestBody.length());
        }

        return request;
    }

    private static String formatRequestBody(String requestBody) {
        if (requestBody.contains("'")) {
            return requestBody.substring(1, requestBody.length() - 1);
        }
        return requestBody;
    }

    private static String readFile(String path) {
        StringBuffer buffer = new StringBuffer();
        try {
            InputStream input = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line.trim());
                line = reader.readLine();
            }

            reader.close();
            input.close();
        } catch (IOException e) {
            System.out.println("IO fail");
        }
        return buffer.toString();
    }

    public static String getHostFromUrl(String url) {
        String[] splitUrl = url.split("//");
        String hostAndPath = splitUrl[1];
        String[] splitHostAndPath = hostAndPath.split("/");
        String host = splitHostAndPath[0];
        return host;
    }

    public static String getPathFromUrl(String url, String host) {
        int position = url.indexOf(host);
        String path = url.substring(position + host.length(), url.length());
        return path;
    }


    private static String findTarget(String target, String[] cmdArray) {
        for (int i = 0; i < cmdArray.length; i++) {
            if (cmdArray[i].equals(target)) {
                if ((i + 1) < cmdArray.length &&
                        !cmdArray[i + 1].equals("-v") &&
                        !cmdArray[i + 1].equals("-h") &&
                        !cmdArray[i + 1].equals("-d") &&
                        !cmdArray[i + 1].equals("-f")) {
                    return cmdArray[i + 1];
                }
            }
        }

        String notFind = "notFind";
        return notFind;
    }

    public static void writeFile(String content, String path) throws IOException {
        File writename = new File(path);
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));
        out.write(content);
        out.flush();
        out.close();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select Test Mode: 1-single thread, 2-multi-threads");
        int option = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Input your request");
        String cmd = scanner.nextLine();
        while (!cmd.equals("exit")) {
            CurlCommandLine commandLine = parseCurlCommandLine(cmd);

            HttpLibrary httpLibrary = new HttpLibrary(commandLine);
            if (option == 1) {
                if (!commandLine.isHelp()) {
                    Response response = httpLibrary.send();
                    if (commandLine.output) {
                        writeFile(response.body, commandLine.outputFile);
                    }
                    if (commandLine.verbose) {
                        System.out.println(response.toString());
                    } else {
                        System.out.println(response.body);
                    }
                }
            } else {
                System.out.println("Input your second request");
                String cmd1 = scanner.nextLine();
                CurlCommandLine commandLine1 = parseCurlCommandLine(cmd1);
                HttpLibrary httpLibrary1 = new HttpLibrary(commandLine1);
                CompletableFuture<Response> response = CompletableFuture.supplyAsync(() -> httpLibrary.send());
                CompletableFuture<Response> response1= CompletableFuture.supplyAsync(()->httpLibrary1.send());
                response.thenAccept(reply-> System.out.println(reply.toString()));
                response1.thenAccept(reply1-> System.out.println(reply1.toString()));
            }
            System.out.println("Select Test Mode: 1-single thread, 2-multi-threads");
            option = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Input your request");
            System.out.println("Input 'exit' to stop.");
            cmd = scanner.nextLine();
        }
    }
}
