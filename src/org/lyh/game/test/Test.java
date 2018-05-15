package org.lyh.game.test;

import javax.sound.sampled.LineUnavailableException;

import org.lyh.game.frame.FiveChessFrame;

public class Test {
	
	private static final int WIDTH = 650; //游戏界面的宽高
	private static final int HEIGHT = 600;

	public static void main(String[] args){
		
		/* 获取本机的所有可用字体
			String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			for (String f : fonts) {
				System.out.println(f);
			}
		*/
		try {
			new FiveChessFrame(WIDTH, HEIGHT); // 启动游戏
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

	}

}
