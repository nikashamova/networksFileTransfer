package nsu.fit.shamova;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {

    public static void main (String[] args) {
        for (int i = 1; i <= 15; i++)
        new Thread(new Client(i)).start();
         //new Thread(new Client()).start();
    }
    private int num = 0;

    public Client(int num){
        this.num = num;
    }

    public Client(){
    }

    public void run() {
        SocketChannel socketChannel = null;
        RandomAccessFile aFile = null;
        try {
            socketChannel = createSocketChannel();
            String fileName;
            if (num != 0) fileName = "in/" + String.valueOf(num) + ".txt";
            else fileName = "in/1.tar.gz";
            aFile = new RandomAccessFile(fileName, "r");
            FileChannel inChannel = aFile.getChannel();

            System.out.println(fileName);

            //writting length of file's name
            ByteBuffer byteFileNameLen = ByteBuffer.allocate(4);
            byteFileNameLen.putInt(fileName.length());
            byteFileNameLen.flip();
            int k = 0;
            while (k < 4) {
                k += socketChannel.write(byteFileNameLen);
            }

            //writing file name
            k = 0;
            byte[] byteFileName = fileName.getBytes();
            ByteBuffer bName = ByteBuffer.wrap(byteFileName);
            while (k < fileName.length()) {
                k += socketChannel.write(bName);
            }

            ByteBuffer fileLen = ByteBuffer.allocate(Long.BYTES);
            long len = aFile.length();
            fileLen.putLong(len);
            fileLen.flip();
            k = 0;
            while (k < Long.BYTES) {
                k+= socketChannel.write(fileLen);
            }
            //writting file
            int a;
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            while ((a = inChannel.read(buffer)) >= 0) {
                buffer.flip();
                while (a > 0) {
                    a-= socketChannel.write(buffer);
                }
                buffer.clear();
            }
            System.out.println("End of writing");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }
                if (aFile != null) {
                    aFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SocketChannel createSocketChannel() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            SocketAddress host = new InetSocketAddress("localhost", 9999);
            socketChannel.connect(host);
            System.out.println("Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socketChannel;
    }

}