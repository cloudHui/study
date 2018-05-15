package org.lyh.game.frame;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class BgMusic implements Runnable{

	@Override
	public void run() {
		
		try {
			
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File("res/hysxm.wav"));
            AudioFormat aif = ais.getFormat(); // 获得音频流的格式
            System.out.println(aif);
            
            final SourceDataLine sdl;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, aif);
            sdl = (SourceDataLine) AudioSystem.getLine(info);
            sdl.open(aif);
            sdl.start();
            FloatControl fc=(FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
            //value可以用来设置音量，从0-2.0
            double value=2;
            float dB = (float)(Math.log(value==0.0?0.0001:value)/Math.log(10.0)*20.0);
            fc.setValue(dB);
            int nByte = 0;
            final int SIZE=1024*64;
            byte[] buffer = new byte[SIZE];
            nByte = ais.read(buffer, 0, SIZE);
			while (nByte != -1) {
				sdl.write(buffer, 0, nByte);
				nByte = ais.read(buffer, 0, SIZE);
			}
			sdl.stop();
	        
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

}
