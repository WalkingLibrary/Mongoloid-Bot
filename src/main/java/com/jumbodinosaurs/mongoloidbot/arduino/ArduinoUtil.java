package com.jumbodinosaurs.mongoloidbot.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import com.jumbodinosaurs.mongoloidbot.arduino.exception.InitializationException;
import com.jumbodinosaurs.mongoloidbot.arduino.exception.PhotoTimeoutException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;

public class ArduinoUtil
{
    private static SerialPort arduinoCamPort;
    
    public static void init()
            throws InitializationException
    {
        
        /*
         * Process for Connecting the Arduino
         * Get Arduino Serial Port
         * Verify/Validate Peripherals Connection
         *
         */
        
        
        /*
         * Process for getting the Arduino Serial Port
         * Note: The Arduino Should Respond with arduinoResponseCode when it receives the arduinoCode
         * The baud Rate of the Arduino Should be 115200
         * Got Though Ports gathered from Serial J Library
         * Send and Receive Messages with timeout
         *
         *
         */
        
        
        System.out.println("Scanning for Arduino...");
        String arduinoCode = "SYN";
        String arduinoResponseCode = "ACK";
        int baudRate = 115200;
        //Get comm port
        SerialPort[] ports = SerialPort.getCommPorts();
        
        //List The ports
        for(SerialPort port : ports)
        {
            System.out.println("Scanning Port [" + port.getSystemPortName() + "]");
            port.setBaudRate(baudRate);
            if(!port.openPort())
            {
                System.out.println("Error opening Port " + port.getSystemPortName());
                continue;
            }
            port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 1000, 1000);
            
            
            boolean[] isArduinoPort = {false};
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream scanner = port.getInputStream();
                    while(port.isOpen() && arduinoCamPort == null)
                    {
                        try
                        {
                            while(scanner.available() >= 1)
                            {
                                outputStream.write(scanner.read());
                            }
                            
                            if(outputStream.size() > 0)
                            {
                                String currentMessage = outputStream.toString();
                                System.out.println("Message Received: " + currentMessage);
                                
                                if(currentMessage.contains(arduinoResponseCode))
                                {
                                    isArduinoPort[0] = true;
                                }
                                outputStream.reset();
                            }
                        }
                        catch(Exception e)
                        {
                            //Do nothing
                        }
                    }
                    System.out.println("No Longer Listening to [" + port.getSystemPortName() + "]");
                }
            };
            
            Thread thread = new Thread(runnable);
            thread.start();
            int timesTried = 0;
            int timesToTry = 4;
            while(timesTried < timesToTry)
            {
                try
                {
                    timesTried++;
                    if(isArduinoPort[0])
                    {
                        arduinoCamPort = port;
                        sendMessage(arduinoCamPort, arduinoResponseCode);
                        Thread.sleep(1000);
                        break;
                    }
                    
                    
                    System.out.println("Trying [" +
                                       port.getSystemPortName() +
                                       "] :" +
                                       timesTried +
                                       " with " +
                                       arduinoCode);
                    sendMessage(port, arduinoCode);
                    
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    //Do Nothing
                }
            }
            
            
            if(arduinoCamPort != null)
            {
                break;
            }
            
            
            port.closePort();
        }
        
        if(arduinoCamPort == null)
        {
            throw new InitializationException("No Arduino Found");
        }
        
        
        System.out.println("Port Found [" + arduinoCamPort.getSystemPortName() + "]");
        //Verify/Validate Peripherals Connection
        try
        {
            
            toggleLight(Light.BLUE);
            toggleLight(Light.RED);
            toggleLight(Light.GREEN);
            takePhoto();
        }
        catch(PhotoTimeoutException | IOException e)
        {
            throw new InitializationException("Camera Took to Long To Take A Photo");
        }
        
        //
        //
        //        Testing Code
        //        while(true)
        //        {
        //            String string = OperatorConsole.getEnsuredAnswer();
        //
        //            if(string.equals("blue"))
        //            {
        //                try
        //                {
        //                    toggleLight(Light.BLUE);
        //                }
        //                catch(IOException e)
        //                {
        //                    e.printStackTrace();
        //                }
        //            }
        //
        //            if(string.equals("green"))
        //            {
        //                try
        //                {
        //                    toggleLight(Light.GREEN);
        //                }
        //                catch(IOException e)
        //                {
        //                    e.printStackTrace();
        //                }
        //            }
        //
        //            if(string.equals("red"))
        //            {
        //                try
        //                {
        //                    toggleLight(Light.RED);
        //                }
        //                catch(IOException e)
        //                {
        //                    e.printStackTrace();
        //                }
        //            }
        //        }
    }
    
    public static void toggleLight(Light light)
            throws IOException
    {
        int code = light.on ? light.offCode : light.onCode;
        
        System.out.println("Turning " + light.name() + (light.on ? "Off" : "On"));
        light.on = !light.on;
        sendMessage(arduinoCamPort, code + "");
    }
    
    public static void sendMessage(SerialPort port, String message)
            throws IOException
    {
        try
        {
            Thread.sleep(750);
            OutputStream output = port.getOutputStream();
            output.write(message.getBytes());
            output.flush();
            Thread.sleep(750);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        
    }
    
    public static BufferedImage takePhoto()
            throws IOException, PhotoTimeoutException
    {
        return takePhoto(0);
    }
    
    private static BufferedImage takePhoto(int photoAttempts)
            throws PhotoTimeoutException, IOException
    {
        String takePhotoCommand = "16";
        sendMessage(arduinoCamPort, takePhotoCommand);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream scanner = arduinoCamPort.getInputStream();
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        boolean photoPhase = false;
        LocalDateTime deadLine = LocalDateTime.now().plusSeconds(10);
        String overallMessage = "";
        while(LocalDateTime.now().isBefore(deadLine))
        {
            try
            {
                while(scanner.available() >= 1)
                {
                    outputStream.write(scanner.read());
                }
                
                if(outputStream.size() > 0)
                {
                    byte[] currentBytes = outputStream.toByteArray();
                    String currentMessage = new String(currentBytes);
                    overallMessage += currentMessage;
                    if(currentMessage.contains("STARTSTART") || overallMessage.contains("STARTSTART"))
                    {
                        System.out.println("Starting Photo Transfer");
                        imageBytes = new ByteArrayOutputStream();
                        photoPhase = true;
                        outputStream.reset();
                        System.out.println(currentMessage);
                        continue;
                    }
    
    
                    if(currentMessage.contains("ENDEND") || overallMessage.contains("ENDEND"))
                    {
                        try
                        {
                            BufferedImage preImage = ImageIO.read(new ByteArrayInputStream(imageBytes.toByteArray()));
                            photoPhase = false;
                            outputStream.reset();
                            System.out.println("Photo Done Transferring");
                            return preImage;
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            if(photoAttempts < 2)
                            {
                                return takePhoto(photoAttempts++);
                            }
                        }
                        /* Writing Photo to A File
                        LocalDateTime localDateTime = LocalDateTime.now();
                        String fileName = localDateTime.toString().replaceAll(":", "-");
                        System.out.println(fileName);
                        File jpg = GeneralUtil.checkFor(JDAController.optionsFolder, "" + fileName + ".jpeg");
                        FileOutputStream fileOutputStreamPhoto = new FileOutputStream(jpg);
                        
                        
                        System.out.println("Amount Read: " + imageBytes.toByteArray().length);
                        fileOutputStreamPhoto.write(imageBytes.toByteArray());
                        fileOutputStreamPhoto.close();
                        
                         */
        
                    }
    
                    if(photoPhase)
                    {
                        imageBytes.write(currentBytes);
                    }
    
                    if(!photoPhase)
                    {
                        System.out.println(currentMessage);
                    }
                    outputStream.reset();
                }
            }
            catch(SerialPortTimeoutException e)
            {
                //Do nothing
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        throw new PhotoTimeoutException("Took To Long to Take a Photo");
    }
    
    public enum Light
    {
        //
        RED(137, 136), GREEN(147, 146), BLUE(145, 144);
        
        public int onCode;
        public int offCode;
        public boolean on;
        
        
        Light(int onCode, int offCode)
        {
            this.onCode = onCode;
            this.offCode = offCode;
            this.on = false;
        }
    }
    
}

