package br.com.chris.httprequest;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	public HttpRequest(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			processRequest();
		}catch(Exception e){
			e.printStackTrace();
			//System.out.println(e);
		}
	}
	
	private void processRequest () throws Exception {
		// Pega a referência do input do socket e o fluxo de saída
		InputStream inpStream = socket.getInputStream();
		
		if (!(inpStream.available() > 0) || !(socket.isConnected())) {
			socket.close();
			return;
        }
		
		DataOutputStream outpStream = new DataOutputStream(socket.getOutputStream()); 
		
		// Filtros do fluxo de entrada
		
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(inpStream));
		
		// Pega a linha inicial (Request-line) da mensagem da requisição HTTP
		String requestLine = buffReader.readLine();
		
		// Imprime a linha inicial (Request-line) 
		System.out.println();
		System.out.println(requestLine);
		
		// Pega o cabeçalho (Header-lines) e o imprime
		String headerLine = null;
		
		// Será zero quando a última linha que termina o header for lida
		while((headerLine = buffReader.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
		// Fechando os fluxos e conexão com o socket
		//outpStream.close();
		//buffReader.close();
		//socket.close();
		
		/*
		 * Agora para mostrar a resposta de forma apropriada
		 * vamos utilizar o nome do arquivo contido na linha de requisicao
		 * Extraindo o nome com StringTokenizer
		*/
		
		// Extraindo o nome do arquivo da requisição
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // pula o primeiro token do "GET"
		String fileName = tokens.nextToken();
		
		/* Coloca-se um "." para que a requisicao do arquivo fique dentro do diretório atual
		 * Isso pois o browser coloca uma barra no início do nome do arquivo 
		*/ 
		System.out.println(fileName);
		if(fileName.equals("/")) {
		
			fileName = "/index.html";
		}
		fileName = "." + fileName;
		
		
		/*
		 * Se o arquivo não for encontrado o construtor do FileInputStream
		 * irá lancar uma FileNotFoundException e para isso vamos usar o try/catch
		 * para ao invés da thread acabar colocarmos nossa variável flag fileExists como false
		 * Utilizando-a mais para frente para construir uma mensagem de erro
		 * Ao invés de enviarmos um arquivo inexistente
		*/
		
		// Abrindo o arquivo requisitado
		FileInputStream fis = null;
		boolean fileExists = true;
		
		try {
			fis = new FileInputStream(fileName);
		}catch(FileNotFoundException e) {
			fileExists = false;
		}
		
		/*
		 * Separando a mensagem de resposta em três partes teremos:
		 * Status Line: Armazenado em uma variável statusLine
		 * Response Headers: Uma resposta única do cabeçalho armazenado em contentTypeLine
		 * Entity Body: Será o corpo HTML que enviaremos caso tenhamos a requisição de um arquivo
		 * inexistente uma mensagem juntamente ao HTML da página
		*/
		
		// Constroi a mensagem de resposta
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		
		if(fileExists) {
			statusLine =  "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
		}		
		else {
			statusLine = "HTTP/1.0 404 NOT FOUND" + CRLF;
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
						"<BODY>Not Found</BODY></HTML>";
		}
		
		// Envia o statusLine
		outpStream.writeBytes(statusLine);
		
		// Envia o contentTypeLine
		outpStream.writeBytes(contentTypeLine);
		
		//Envia uma linha em branco para indicar o fim da linha de cabeçalho
		outpStream.writeBytes(CRLF);
		
		/*
		 * Agora que os três já estão no output stream em direcao ao browser
		 * é hora de fazer o mesmo com o entityBody, caso o arquivo exista será chamado 
		 * um método separado para enviar o arquivo, caso não exista enviaremos uma mensagem de erro
		 * no HTML da página*/
		
		if(fileExists) {
			sendBytes(fis, outpStream);
			fis.close();
		}else {
			outpStream.writeBytes(entityBody + CRLF);
		}
		
		// Fechando os fluxos e conexão com o socket
		outpStream.close();
		buffReader.close();
		socket.close();
		
	}
	
	private static void sendBytes (FileInputStream fis, OutputStream outpStream) throws Exception{
		// Constrói um buffer de 1K para armazenar os bytes até chegarem no socket
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Copia o arquivo requisitado no output stream do socket
		while((bytes = fis.read(buffer)) != -1) {
			outpStream.write(buffer, 0, bytes);
		}
	}

	private static String contentType (String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
			return "text/html";
		if (fileName.endsWith(".gif"))
			return "image/gif";
		if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg"))
			return "image/jpeg";
		if (fileName.endsWith(".mp3"))
			return "audio/mp3";
		if (fileName.endsWith(".mp4"))
			return "video/mp4";
		
		return "application/octet-stream";
	}
	
}
