package Practica;

import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

public class PracticaDawid {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: JavaCompilerApp <archivoFuente.java> <directorioDestino>");
            return;
        }
        
        String filePath = args[0];
        String destinationPath = args[1];
        
        try {
            compileAndMoveClassFile(filePath, destinationPath);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void compileAndMoveClassFile(String filePath, String destinationPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", filePath);
        processBuilder.redirectErrorStream(true);
        
        long startTime = System.currentTimeMillis(); 
        Process process = processBuilder.start();
        

        new Thread(new StreamGobbler(process.getInputStream())).start();
        new Thread(new StreamGobbler(process.getErrorStream())).start();


        int exitCode = process.waitFor();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        

        if (exitCode == 0) {
            System.out.println("Compilación exitosa en " + duration + " ms.");
            String className = new File(filePath).getName().replace(".java", ".class");
            Path sourcePath = Paths.get(className);
            Path destinationDir = Paths.get(destinationPath);
            

            Files.createDirectories(destinationDir);
            Files.move(sourcePath, destinationDir.resolve(className), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Archivo .class copiado a: " + destinationDir.resolve(className));
        } else {
            System.out.println("Compilación fallida en " + duration + " ms.");
        }
    }

    public static void forceTerminationOption(Process process) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Escriba 'exit' para forzar la terminación del proceso.");
        
        while (true) {
            if (scanner.nextLine().equalsIgnoreCase("exit")) {
                process.destroy();
                System.out.println("Proceso terminado por el usuario.");
                break;
            }
        }
        scanner.close();
    }

    static class StreamGobbler implements Runnable {
        private final InputStream inputStream;

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error al leer la salida del proceso: " + e.getMessage());
            }
        }
    }
}

