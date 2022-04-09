package ru.gb.storage.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server().start();
        System.out.println("Server started");
    }

    public void start () throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 9000);


        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();

        serverSocket.socket().bind(socketAddress);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started!");

        while (true){
            selector.select();
            System.out.println("New selector event!");
            Set <SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator <SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if(selectionKey.isAcceptable()){
                    System.out.println("New selector acceptable event");
                    register(selector, serverSocket);
                }
                if (selectionKey.isReadable()){
                    System.out.println("New selector readable event!");
                    readMessageAndEcho(selectionKey);
                }
                iterator.remove();
            }

        }
    }


    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }

    public void readMessageAndEcho(SelectionKey selectionKey) throws IOException{
        SocketChannel client = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        client.read(byteBuffer);
        String message = new String(byteBuffer.array(), "UTF-8");
        String receiveMsg = "New server message: " + message;
        System.out.println(receiveMsg);
        echo(client, message);

    }

    public void echo(SocketChannel client, String message) throws IOException {
        String responseMessage = "echo_" + message;
        ByteBuffer responseBuffer = ByteBuffer.allocate(responseMessage.getBytes().length);
        responseBuffer.put(responseMessage.getBytes());
        responseBuffer.flip();
        client.write(responseBuffer);
    }



}
