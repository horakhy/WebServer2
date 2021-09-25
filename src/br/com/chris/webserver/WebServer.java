package br.com.chris.webserver;

import br.com.chris.httprequest.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author chris
 */
public class WebServer {

    public static void main(String[] args) throws Exception {
        // Set the port number
        int porta = 3000;
        
        // Cria o listener do socket
        try (ServerSocket ss = new ServerSocket(porta)) {
            System.out.println("Servidor esta escutando na porta " + porta);

            while (true) {
                // Listening TCP connections' requests
                Socket clientSocket = ss.accept();
                System.out.println("Novo client conectado");
                
                // Cria o objeto que irá processar a mensagem da requisicao HTTP
                HttpRequest request = new HttpRequest(clientSocket);
                
                // Cria a thread que irá processar a requisição
                Thread thread = new Thread(request);
                
                // Inicia
                thread.start();

            }
        } catch (IOException ex) {
            System.out.println("Excecao lancada " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}


