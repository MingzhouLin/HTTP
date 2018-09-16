import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class HttpClient {

    public static CurlCommandLine parseCurlCommandLine(String cmd){
        CurlCommandLine curlCommandLine = new CurlCommandLine();
        Request request = new Request();

        String[] cmdArray = cmd.split(" ");

        if (cmdArray[0].equals("httpc")){
            curlCommandLine.setValid(true);
        } else{
            curlCommandLine.setValid(false);
        }

        if (cmd.contains("get")){
            curlCommandLine.setRequestType("get");
        }

        if (cmd.contains("post")){
            curlCommandLine.setRequestType("post");
        }

        if (cmd.contains("-v")){
            curlCommandLine.setVerbose(true);
        } else {
            curlCommandLine.setVerbose(false);
        }

        if (cmd.contains("-h")){
            curlCommandLine.setHaveHeaders(true);
            String headerString = findTarget("-h",cmdArray);
            String[] headerSplit = headerString.split(";");
            ArrayList<String> headers = new ArrayList<>();
            for (int i = 0; i < headerSplit.length; i++) {
                headers.add(headerSplit[i]);
            }
            curlCommandLine.setHeaders(headers);
        } else {
            curlCommandLine.setHaveHeaders(false);
            curlCommandLine.setHeaders(new ArrayList<String>());
        }

        if (cmd.contains("-d")){
            curlCommandLine.setHaveInlineData(true);
            String inlineData = findTarget("-d", cmdArray);
            curlCommandLine.setInlineData(inlineData);
        }else {
            curlCommandLine.setHaveInlineData(false);
        }

        if (cmd.contains("-f")) {
            curlCommandLine.setHaveFile(true);
            String file = findTarget("-f",cmdArray);
            curlCommandLine.setFile(file);
        } else {
            curlCommandLine.setHaveFile(false);
        }

        curlCommandLine.setUrl(cmdArray[cmdArray.length - 1]);

        curlCommandLine.setRequest(configRequest(curlCommandLine));

        return curlCommandLine;
    }

    private static Request configRequest(CurlCommandLine curlCommandLine) {
        Request request = new Request();

        String host = getHostFromUrl(curlCommandLine.getUrl());
        String path = getPathFromUrl(curlCommandLine.getUrl(), host);

        request.setPort(80);//default port
        request.setHost(host);
        request.setPath(path);
        request.setRequestHeaders(curlCommandLine.getHeaders());
        request.requestType=curlCommandLine.requestType;
        if (curlCommandLine.haveInlineData){
            request.requestBody= curlCommandLine.inlineData;
        }
        if (curlCommandLine.haveFile){
            request.requestBody+= readFile(curlCommandLine.file);
        }

        return request;
    }

    private static String readFile(String path) {
        StringBuffer buffer = new StringBuffer();
        try {
            InputStream input = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append("\n");
                line = reader.readLine();
            }

            reader.close();
            input.close();
        }catch (IOException e){
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
        String[] splitUrl = url.split(host);
        String path = splitUrl[1];
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String cmd = scanner.nextLine();

        CurlCommandLine commandLine = parseCurlCommandLine(cmd);

        HttpLibrary httpLibrary = new HttpLibrary(commandLine);

        System.out.println(httpLibrary.send());
    }
}
