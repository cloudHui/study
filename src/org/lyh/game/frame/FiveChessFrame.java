package org.lyh.game.frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FiveChessFrame extends JFrame implements MouseListener, Runnable, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width; // 获得屏幕尺寸
    int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

    int x = 0, y = 0; // 棋子坐标

    int[][] allChess = new int[19][19]; // 保存之前下过的棋子的状态，0表示没有棋子，1黑子，2白子

    String message = "黑方先行";

    boolean isBlack = true; // 判断当前是黑棋还是白棋进行下一步

    boolean canPlay = true; // 判断是否可以落下棋子

    BufferedImage bgImage = null; // 背景图片

    int maxTime = 0; // 保存最多拥有的时间（秒）

    Thread thread = new Thread(this); // 倒计时的线程类

    int balckTime = 0, whiteTime = 0; // 黑方和白方的剩余时间

    String balckMessage = "00:00:00", whiteMessage = "00:00:00"; // 保存双方剩余时间的显示信息,默认是0表示无限制

    boolean musicOn = true; // 是否开启背景音乐

    Thread bgMusicThread = new Thread(new BgMusic()); // 背景音乐线程

    Cursor cursor = null; // 十字光标

    @SuppressWarnings("deprecation")
    public FiveChessFrame(int width, int height) throws LineUnavailableException {
        this.setTitle("五子棋游戏");
        this.setSize(width, height);
        this.setLocation((screenWidth - width) / 2, (screenHeight - height) / 2); // 游戏窗体居中显示
        this.setResizable(false); // 窗体大小不可变
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addMouseListener(this); // 注册鼠标监听事件
        this.addMouseMotionListener(this);
        this.setVisible(true);

        cursor = new Cursor();
        thread.start();
        thread.suspend(); //　计时线程挂起
        if (musicOn) {
            bgMusicThread.start();
        }
        this.repaint(); // 刷新屏幕，解决启动游戏时的黑屏问题
    }

    /**
     * 渲染界面元素
     */
    @Override
    public void paint(Graphics g) {
        /* 加载背景图片 */
        try {
            bgImage = ImageIO.read(new File("res/bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean flag = g.drawImage(bgImage, 0, 0, this);
        if (flag) {
            System.out.println("图片加载成功！");
        }

		/* 文字信息的设置 */
        g.setFont(new Font("黑体", Font.BOLD, 20));
        g.setColor(Color.YELLOW);
        g.drawString("游戏信息：" + message, 150, 60);

        g.setFont(new Font("宋体", Font.PLAIN, 18));
        g.setColor(Color.GREEN);
        g.drawString("黑方时间：" + balckMessage, 30, 550);
        g.drawString("白方时间：" + whiteMessage, 300, 550);

        g.setFont(new Font("楷体", Font.BOLD, 18));
        g.setColor(Color.CYAN);
        g.drawRect(530, 100, 100, 30);
        g.drawString("新游戏", 552, 123);
        g.drawRect(530, 170, 100, 30);
        g.drawString("游戏设置", 545, 193);
        g.drawRect(530, 240, 100, 30);
        g.drawString("游戏说明", 545, 263);
        g.drawRect(530, 300, 100, 30);
        g.drawString("音乐" + (musicOn ? "开" : "关"), 545, 320);
        g.drawRect(530, 350, 100, 30);
        g.drawString("认输", 555, 373);
        g.drawRect(530, 420, 100, 30);
        g.drawString("关于", 555, 443);
        g.drawRect(530, 490, 100, 30);
        g.drawString("退出", 555, 513);

        g.setColor(Color.RED);
        g.setFont(new Font("楷体", Font.BOLD, 28));
        g.drawString("你若安好，便是晴天。", 320, 590);

        g.setColor(Color.ORANGE);
        /* 初始化棋盘格(18 * 18) */
        for (int i = 0; i < 19; i++) {
            g.drawLine(15, 70 + 25 * i, 465, 70 + 25 * i); // 绘制横线
            g.drawLine(15 + 25 * i, 70, 15 + 25 * i, 520);// 绘制竖线
        }

        if (cursor.show) {
            g.drawImage(cursor.cursorImage, cursor.point.x - cursor.width / 2, cursor.point.y - cursor.width / 2, null);
        }
		
		/* 标注9个特殊点位  */
        g.setColor(Color.MAGENTA);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                g.fillOval(7 + 3 * 25 + 150 * i, 63 + 3 * 25 + 150 * j, 15, 15);
            }
        }
		
		/* 绘制全部棋子 */
        g.setColor(Color.BLACK);
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {

                int tempX = 25 * i + 15; // 得到棋子的准确坐标
                int tempY = 25 * j + 70;

                cursor.point.x = tempX;
                cursor.point.y = tempY;

                if (allChess[i][j] == 1) {
                    // 绘制黑子
                    g.fillOval(tempX - 11, tempY - 11, 22, 22);
                }
                if (allChess[i][j] == 2) {
                    // 绘制白子
                    g.setColor(Color.WHITE);
                    g.fillOval(tempX - 11, tempY - 11, 22, 22);
                    g.setColor(Color.BLACK);
                    g.drawOval(tempX - 11, tempY - 11, 22, 22);
                }
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

        cursor.show = false;
        x = e.getX(); // 得到鼠标的点击位置
        y = e.getY();
        System.out.println("点击坐标：(" + x + "," + y + ")");

        if (canPlay) {
			/* 判断点击的坐标是否在18*18的范围之内 */
            if (x >= 15 && x <= 465 && y >= 70 && y <= 520) {
//				x = (x - 15) / 25; // 得到最近的十字点,可以先得到0~18的整数，然后通过这个整数乘以每行的间距完成
//				y = (y - 70) / 25;
				
				/* 以上的判定存在误差，如果x坐标大于单元格的中线，棋子应该落入右侧的十字线，y坐标同理 */
                if (((x - 15) % 25) >= 13) {
                    x = ((x - 15) / 25) + 1;
                } else
                    x = (x - 15) / 25;
                if (((y - 70) % 25) >= 13) {
                    y = ((y - 70) / 25) + 1;
                } else
                    y = (y - 70) / 25;

                System.out.println("x = " + x + ", y = " + y);
                System.out.println("allChess[x][y] = " + allChess[x][y]);

                if (allChess[x][y] == 0) {
					/* 判断当前要下的是什么棋子？ */
                    if (isBlack) {
                        allChess[x][y] = 1;
                        isBlack = false;
                        message = "轮到白方";
                    } else {
                        allChess[x][y] = 2;
                        isBlack = true;
                        message = "轮到黑方";
                    }

					/* 判断当前棋子是否和其他棋子5连？ */
                    boolean winFlag = this.checkWin();
                    if (winFlag) {
                        int result = JOptionPane.showConfirmDialog(this, "游戏结束," + (allChess[x][y] == 1 ? "黑方赢了" : "白方赢了") + "3秒钟后自动继续。。。");
                        canPlay = false;
                        //停顿3秒询问是否开始下一场
                        try {
                            Thread.sleep(3000);
                            if (JOptionPane.OK_OPTION == result) {
                                clearAllCheese();
                            }
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                    }
                } else {
                    JOptionPane.showMessageDialog(this, "当前位置已有棋子，请重新落子！");
                }
                this.repaint();
            }
        } else if (!(x >= 530 && x <= 630 && y >= 100 && y <= 490)) {
            //有玩家已经赢了弹提示是否重新开始
            int result = JOptionPane.showConfirmDialog(this, "游戏已经结束,是否重新开始游戏？");
            if (JOptionPane.OK_OPTION == result) {
                clearAllCheese();
            }
        }

        // 点击开始新游戏按钮
        if (x >= 530 && x <= 630 && y >= 100 && y <= 130) {
            int result = JOptionPane.showConfirmDialog(this, "开始新游戏，棋盘棋子将清空，确认开始新游戏？");
            if (JOptionPane.OK_OPTION == result) {
                clearAllCheese();
            }
        }
        // 点击游戏设置按钮
        if (x >= 530 && x <= 630 && y >= 170 && y <= 200) {
            String input = JOptionPane.showInputDialog("请输入游戏时间（单位：分钟），输入0表示没有时间限制：");
            if (null == input || "" == input) {
                JOptionPane.showMessageDialog(this, "输入为,请输入正确信息！");
            } else {
                try {
                    maxTime = Integer.parseInt(input) * 60;
                    while (maxTime < 0) {
                        JOptionPane.showMessageDialog(this, "请输入正确信息，不允许输入负数！");
                        input = JOptionPane.showInputDialog("请输入游戏时间（单位：分钟），输入0表示没有时间限制：");
                        maxTime = Integer.parseInt(input) * 60;
                    }
                    if (maxTime == 0) {
                        int result = JOptionPane.showConfirmDialog(this, "设置完成,是否重新开始游戏？");
                        if (JOptionPane.OK_OPTION == result) {
                            clearAllCheese();
                        }
                    }
                    if (maxTime > 0) {
                        int result = JOptionPane.showConfirmDialog(this, "设置完成,是否重新开始游戏？");
                        if (JOptionPane.OK_OPTION == result) {
                            clearAllCheese();
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "输入无效，请重试！" + ex);
                }
            }
        }
        // 点击游戏说明按钮
        if (x >= 530 && x <= 600 && y >= 240 && y <= 270) {
            JOptionPane.showMessageDialog(this, "这是一个五子棋游戏程序,黑白双方轮流下棋,当某一方连到5子时获胜", "游戏简介", JOptionPane.INFORMATION_MESSAGE);
        }
        // 点击认输按钮
        if (x >= 530 && x <= 630 && y >= 350 && y <= 380) {
            if (!canPlay) {
                JOptionPane.showMessageDialog(this, "游戏已经结束！" + (isBlack ? "黑方已经赢了" : "白方已经赢了"));
            } else {
                int result = JOptionPane.showConfirmDialog(this, "确认认输？");
                if (JOptionPane.OK_OPTION == result) {
                    if (isBlack) {
                        JOptionPane.showMessageDialog(this, "黑方已认输，白方获胜！\n游戏结束！");
                    } else {
                        JOptionPane.showMessageDialog(this, "白方已认输，黑方获胜！\n游戏结束！");
                    }
                    canPlay = false;
                    this.repaint();
                }
            }
        }
        // 点击关于按钮
        if (x >= 530 && x <= 630 && y >= 420 && y <= 450) {
            JOptionPane.showMessageDialog(this, "本游戏由\"易水萧萧\"制作,欢迎访问我的博客http://www.cnblogs.com/happyfans/", "关于游戏", JOptionPane.PLAIN_MESSAGE);
        }
        // 点击退出按钮
        if (x >= 530 && x <= 630 && y >= 490 && y <= 520) {
            gameExit();
        }
        // 点击背景音乐按钮
        if (x >= 530 && y <= 630 && y >= 300 && y <= 330) {
            musicOn = !musicOn;
            if (!musicOn) {
                bgMusicThread.suspend();
            } else {
                bgMusicThread.resume();
            }
            this.repaint();
        }

    }

    /**
     * 游戏退出
     */
    private void gameExit() {
        JOptionPane.showMessageDialog(this, "再见！", "游戏退出", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
    }

    /**
     * 清空棋盘，重新绘图
     */
    @SuppressWarnings("deprecation")
    private void clearAllCheese() {
		/* 1.清空棋盘，allChess[][]二维数组数据全部置为0 */
        canPlay = true;
        // 方式一：
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                allChess[i][j] = 0;
            }
        }
        // 方式二：
//		allChess = new int[19][19]; // 产生垃圾
		
		/* 2.将游戏信息的显示改回到"黑方先行"  */
        message = "黑方先行";
		
		/* 3.将下一步下棋的人改为黑方 */
        isBlack = true;
        balckTime = whiteTime = maxTime;
        balckMessage = whiteMessage = getFormatedTime(maxTime);

        thread.resume(); // 重新启动线程
        this.repaint(); // 重新绘图
    }

    /**
     * 判断输赢,一共有4个方向水平、垂直、两条对角线
     */
    private boolean checkWin() {

        boolean flag = false;
        int count = 1; // 保存一共有多少个相同颜色的棋子相连，默认值是1，它自己是一个
		
		/* 水平方向上是否有5个棋子相连，特点是垂直方向的坐标相同，allCheese[x][y]中y值相同 */
        int color = allChess[x][y]; // 当前所下的棋子是黑色还是白色？
		
		/*
		 * 这个判断是不合理的，因为一直判断下去，程序无法退出
			if (color == allChess[x+1][y]) {
				count++;
				if (color == allChess[x+2][y]) {
					count++;
					if (color == allChess[x+3][y]) {
						count++;
					}
				}
			}
		*/
		
		/* 通过循环来判断棋子是否相连的判断 */
		
		/*
		 * 这部分代码冗余度很大——同样的代码写了4次
			// 横向判断
			int i = 1;
			while (color==allChess[x+i][y]) { // 向右判断 ---> allChess[x+i][y+0]
				count++;
				i++;
			}
			i = 1; // i归位
			while (color == allChess[x-i][y]) { // 向左判断---> allChess[x-i][y+0]
				count++;
				i++;
			}
			if (count >= 5) {
				flag = true;
			}
			
			// 纵向判断（和横向的判断一样）
			int i2 = 1;
			int count2 = 1;
			while (color==allChess[x][y+i2]) { // 向下判断  ---> allChess[x+0][y+i2]
				count2++;
				i2++;
			}
			i2 = 1; // i归位
			while (color == allChess[x][y-i2]) { // 向上判断 ---> allChess[x+0][y-i2]
				count2++;
				i2++;
			}
			if (count2 >= 5) {
				flag = true;
			}
			
			// 左下-右上判断
			int i3 = 1;
			int count3 = 1;
			while (color==allChess[x+i3][y-i3]) { // 左下--->右上---> allChess[x+i3][y-i3]
				count3++;
				i3++;
			}
			i3 = 1; // i3归位
			while (color == allChess[x-i3][y+i3]) { // 右上--->左下---> allChess[x-i3][y+i3]
				count3++;
				i3++;
			}
			if (count3 >= 5) {
				flag = true;
			}
			
			// 左上-右下判断
			int i4 = 1;
			int count4 = 1;
			while (color==allChess[x+i4][y+i4]) { // 左上--->右下---> allChess[x+i4][y+i4]
				count4++;
				i4++;
			}
			i4 = 1; // i3归位
			while (color == allChess[x-i4][y-i4]) { // 右下--->左上---> allChess[x-i4][y-i4]
				count4++;
				i4++;
			}
			if (count4 >= 5) {
				flag = true;
			}
		*/
		
		/* 用以下的代码判断4个方向的棋子数简化了很多 */
        count = checkCount(1, 0, color); // 横向的棋子数
        if (count >= 5) {
            flag = true;
        } else {
            count = checkCount(0, 1, color); // 纵向的棋子数
            if (count >= 5) {
                flag = true;
            } else {
                count = checkCount(1, -1, color); // 左下-右上的棋子数
                if (count >= 5) {
                    flag = true;
                } else {
                    count = checkCount(1, 1, color); // 左上-右下的棋子数
                    if (count >= 5) {
                        flag = true;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * 得到相同的棋子连接的数量,由上面的4次判断抽象出来的
     *
     * @param xChange:x的变化
     * @param yChange：y的变化
     * @param color：当前棋子状态（黑？白）
     * @return
     */
    private int checkCount(int xChange, int yChange, int color) {

        int count = 1;
        int tempX = xChange, tempY = yChange; // 保存传过来的xChange和yChange，复位的时候需要用到
        // 首先要检查数组下标是否越界
        while (x + xChange >= 0 && x + xChange <= 18 && y + yChange >= 0 && y + yChange <= 18 && color == allChess[x + xChange][y + yChange]) {
            count++;
            if (xChange != 0) {
                xChange++;
            }
            if (yChange != 0) {
                if (yChange > 0) {
                    yChange++;
                } else {
                    yChange--;
                }
            }
        }

        xChange = tempX; // 复位
        yChange = tempY;
        // 首先要检查数组下标是否越界
        while (x - xChange >= 0 && x - xChange <= 18 && y - yChange >= 0 && y - yChange <= 18 && color == allChess[x - xChange][y - yChange]) {
            count++;
            if (xChange != 0) {
                xChange++;
            }
            if (yChange != 0) {
                if (yChange > 0) {
                    yChange++;
                } else {
                    yChange--;
                }
            }
        }

        return count;
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void run() {

        // 是否有时间的限制？
        if (maxTime > 0) {
            while (true) {

                if (isBlack) {
                    balckTime--;
                } else {
                    whiteTime--;
                }

                if (whiteTime == 0) {
                    JOptionPane.showMessageDialog(this, "白方超时，黑方获胜！");
                    canPlay = false;
                }
                if (balckTime == 0) {
                    JOptionPane.showMessageDialog(this, "黑方超时，白方获胜！");
                    canPlay = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("黑方时间\t白方时间  【单位：秒】");
                System.out.println(balckTime + "\t" + whiteTime);

                balckMessage = getFormatedTime(balckTime);
                whiteMessage = getFormatedTime(whiteTime);
                this.repaint();

            }
        }
    }

    /**
     * 返回格式化的时分秒即：hh:MM:ss
     *
     * @param second
     * @return
     */
    private static String getFormatedTime(int second) {

        int currentSecond = second % 60;
        int totalMinute = second / 60;
        int currentMinute = totalMinute % 60;
        int totalHour = totalMinute / 60;
        return checkTime(totalHour) + ":" + checkTime(currentMinute) + ":" + checkTime(currentSecond);
    }

    /**
     * 如果时间小于1位数，前面补0
     *
     * @param number
     * @return
     */
    private static String checkTime(int number) {

        String result = "";
        if (number < 0) {
            result = "00";
        } else {
            result = number <= 9 ? "0" + number : "" + number;
        }
        return result;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        if (x >= 15 && x <= 465 && y >= 70 && y <= 520) {
			/* 以上的判定存在误差，如果x坐标大于单元格的中线，棋子应该落入右侧的十字线，y坐标同理 */
            if (((x - 15) % 25) >= 13) {
                x = ((x - 15) / 25) + 1;
            } else
                x = (x - 15) / 25;
            if (((y - 70) % 25) >= 13) {
                y = ((y - 70) / 25) + 1;
            } else
                y = (y - 70) / 25;

            cursor.point.x = 15 + x * 25;
            cursor.point.y = 70 + y * 25;
            cursor.show = true;
        } else {
            cursor.show = false;
        }
//		repaint();//鼠标移动不用重新绘制面板
    }
}
