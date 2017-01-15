package nsu.fit.shamova;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    public int BUFFERSIZE = 1024;

    public static void main (String[] args) {
        Server server = new Server();
        server.foo();
    }
    Server() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(9999));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void foo() {
        //configure();
        while (true){
            try {
                int noOfKeys = selector.select();
                if (noOfKeys == 0) {
                    continue;

                }
                Set selectedKeys = selector.selectedKeys();
                Iterator iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();

                    if (key.isAcceptable()) {
                        SocketChannel clientChannel;
                        clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ, new AttachmentInfo());

                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        readFile(key);
                        channel.close();
                    }
                    iter.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void readFile(SelectionKey key) throws IOException {

        AttachmentInfo info = (AttachmentInfo) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();

        if (info.inputBuffer == null) {
            info.inputBuffer = ByteBuffer.allocate(BUFFERSIZE);
            info.inputBuffer.clear();
        }
        if (socketChannel.read(info.inputBuffer) < 0) {
            socketChannel.close();
            info.fileChannel.close();
        } else {
            info.inputBuffer.flip();
            if (info.nameSizeBuffer.hasRemaining()) {
                while (info.nameSizeBuffer.hasRemaining() && info.inputBuffer.hasRemaining()) {
                    byte b = info.inputBuffer.get();
                    info.nameSizeBuffer.put(b);
                }
                //info.nameSizeBuffer.put(info.inputBuffer.array(), offset, wr);
            }
            if (!info.nameSizeBuffer.hasRemaining()) {
                if (info.nameBuffer == null) {
                    info.nameSizeBuffer.flip();
                    info.setNameSize(info.nameSizeBuffer.getInt());
                    info.nameBuffer = ByteBuffer.allocate(info.getNameSize());
                }
                if (info.nameBuffer.hasRemaining()) {
                    while (info.nameBuffer.hasRemaining() && info.inputBuffer.hasRemaining()) {
                        byte b = info.inputBuffer.get();
                        info.nameBuffer.put(b);
                    }
                    //info.nameBuffer.put(info.inputBuffer);
                }
                if (!info.nameBuffer.hasRemaining()) {
                    info.setName(new String(info.nameBuffer.array()));
                    String xFileName = "out/" + info.getName();
                    int count = 0;
                    if (new File(xFileName).exists()) {
                        while (new File((xFileName + "(" + count + ")")).exists()) {
                            count++;
                        }
                        xFileName += ("(" + count + ")");
                    }
                    RandomAccessFile aFile = null;
                    try {
                        aFile = new RandomAccessFile(xFileName, "rw");
                    }
                    catch (SecurityException e) {
                        System.out.println("Permission denied!!!!");
                        return;
                    }
                    info.fileChannel = aFile.getChannel();

                    if (info.fileSizeBuffer.hasRemaining()) {
                        while (info.fileSizeBuffer.hasRemaining() && info.inputBuffer.hasRemaining()) {
                            byte b = info.inputBuffer.get();
                            try {
                                info.fileSizeBuffer.put(b);
                            }
                            catch (ReadOnlyBufferException e) {
                                System.out.println("READ ONLY!!!");
                                info.fileChannel.close();
                                return;
                            }
                        }
                    } if(!info.fileSizeBuffer.hasRemaining()) {
                        if (info.fileBuffer == null) {
                            info.fileSizeBuffer.flip();
                            info.setFileSize(info.fileSizeBuffer.getLong());
                            info.fileBuffer = ByteBuffer.allocate(info.getFileSize().intValue());
                        }
                        if (info.fileBuffer.hasRemaining()) {
                            while (info.fileBuffer.hasRemaining() && info.inputBuffer.hasRemaining()) {
                                byte b = info.inputBuffer.get();
                                info.fileBuffer.put(b);
                            }
                            //info.fileBuffer.put(info.inputBuffer);
                        }
                        if (info.fileBuffer.remaining() == 0) {
                            info.fileBuffer.flip();
                            info.fileChannel.write(info.fileBuffer);
                            info.fileBuffer.clear();
                        }
                    }
                }
            }

            info.inputBuffer.clear();
        }
    }
}