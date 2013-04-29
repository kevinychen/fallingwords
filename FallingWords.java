/* Falling Words - @ Kevin Y. Chen */

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.Timer;
import java.util.List;

public final class FallingWords
{
	static JFrame frame;

	static final int EASY = 0;
	static final int MEDIUM = 1;
	static final int HARD = 2;
	static final int DEATH = 3;

	private static final ArrayList<String> easy;
	private static final ArrayList<String> medium;
	private static final ArrayList<String> hard;
	private static final ArrayList<String> death;

	static
	{
		easy = new ArrayList<String>();
		medium = new ArrayList<String>();
		hard = new ArrayList<String>();
		death = new ArrayList<String>();

		try
		{
			Scanner in = new Scanner(new File("dict_full.txt"));
			
			while (in.hasNext())
			{
				String current = in.next().toLowerCase();
				if (current.length() >= 3)
				{
					if (current.length() <= 4)
						easy.add(current);
					else if (current.length() <= 6)
						medium.add(current);
					else if (current.length() <= 9)
						hard.add(current);
					else if (current.length() <= 12)
						death.add(current);
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("FILE NOT FOUND");
			System.exit(0);
		}
	}

	static String randomWord(int difficulty)
	{
		if (difficulty == 0)
			return easy.get((int)(Math.random() * easy.size()));
		if (difficulty == 1)
			return medium.get((int)(Math.random() * medium.size()));
		if (difficulty == 2)
			return hard.get((int)(Math.random() * hard.size()));
		if (difficulty == 3)
			return death.get((int)(Math.random() * death.size()));

		return null;
	}
	
	static void lost(int score)
	{
		JOptionPane.showMessageDialog(null, "YOU LOSE.\nYour score is " + score);
		displayScoreboard(score);
	}

	static void win(int score)
	{
		JOptionPane.showMessageDialog(null, "YOU WIN!\nYour score is " + score);
		displayScoreboard(score);
	}

	static void displayScoreboard(int score)
	{
		frame.dispose();

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(size.width / 3, size.height - 50);
		frame.setLocation(size.width / 3, 0);
		frame.setResizable(false);
		Scoreboard scoreboard = new Scoreboard("highscores_fallingwords.txt");

		if (scoreboard.makesList(score))
		{
			String name = JOptionPane.showInputDialog(null, "YOU MADE THE HIGH SCORE LIST! =D PLEASE ENTER NAME:");
			
			name.trim();
			if (name.length() > 0)
			{
				if (name.length() > 30)
					name = name.substring(0, 30);
				
				scoreboard.addScore(name, score);
				scoreboard.write();
			}
		}
		frame.add(scoreboard);
		JButton button = new JButton("MAIN MENU");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				menu();
			}
		});
		frame.add(button, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	static void menu()
	{
		frame.dispose();

		frame = new JFrame("FALLING WORDS!!! =D");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(size.width / 3, size.height);
		frame.setLocation(size.width / 3, 0);
		frame.setResizable(false);
		JButton button = new JButton("CLICK TO START");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				start();
			}
		});
		frame.add(button);
		frame.setVisible(true);
	}

	static void start()
	{
		frame.dispose();

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(size.width / 3, size.height);
		frame.setLocation(size.width / 3, 0);
		frame.setResizable(false);
		MyPanel panel = new MyPanel();
		frame.add(panel);
		frame.setVisible(true);
		
		panel.start();
	}

	public static void main(String ... kevy)
	{
		frame = new JFrame();
		menu();
	}
}

final class MyString
{
	final String string;
	final int x;
	final int difficulty;
	int y;

	MyString(String string, int x, int y, int difficulty)
	{
		this.string = string;
		this.x = x;
		this.difficulty = difficulty;
		this.y = y;
	}

	void setY(int change)
	{
		y += change;
	}
}

final class MyPanel extends JPanel
{
	static final double DROP_PROBABILITY = .05;
	static final int LEVEL_CHANGE = 50;
	static final int SPEED = 50;

	final Queue<MyString> words;
	final Timer timer;
	StringBuilder typed;
	int score;
	int difficulty;
	int counter;
	boolean controlOn;

	MyPanel()
	{
		words = new LinkedList<MyString>();
		typed = new StringBuilder();
		score = 0;
		difficulty = FallingWords.EASY;
		counter = 0;

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				{
					if (controlOn)
						typed.setLength(0);
					else if (typed.length() != 0)
						typed.deleteCharAt(typed.length() - 1);
				}
				else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					controlOn = true;
				}
				else if (typed.length() <= 26)
				{
					typed.append((char)e.getKeyCode());
				}
				repaint();
			}
			
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
					controlOn = false;
			}
		});

		ActionListener listener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				act();
				
				Iterator<MyString> it = words.iterator();
				while (it.hasNext())
				{
					MyString next = it.next();
					if (typed.toString().equalsIgnoreCase(next.string))
					{
						it.remove();
						score += next.difficulty + 1;
						typed.setLength(0);
					}
				}
				
				if (words.isEmpty() && difficulty > FallingWords.DEATH)
				{
					FallingWords.win(10 * LEVEL_CHANGE);
					timer.stop();
				}
				
				repaint();
			}
		};
		timer = new Timer(SPEED, listener);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		for (MyString word : words)
		{
			if (word != words.peek())
			{
				g.setColor(Color.BLACK);
				g.setFont(new Font("Times New Roman", 0, 16));
				g.drawString(word.string, word.x, word.y);
			}
		}
		g.drawLine(0, getSize().height * 5 / 6, getSize().width, getSize().height * 5 / 6);
		g.setColor(Color.BLUE);
		g.setFont(new Font("Times New Roman", 0, 20));
		if (words.peek() != null)
		{
			g.drawString(words.peek().string, words.peek().x, words.peek().y);
			g.drawString(words.peek().string, 40, getSize().height - 80); // the current word
		}
		g.drawString(typed.toString(), 40, getSize().height - 40); // the typer's text
		g.drawString("SCORE: " + score, getSize().width - 200, getSize().height - 60);
	}

	public boolean isFocusable()
	{
		return true;
	}

	void start()
	{
		while (words.isEmpty() || words.peek().y < getSize().height / 2)
			act();
		
		timer.start();
	}
	
	void act()
	{
		for (MyString word : words)
		{
			word.setY(1);
			if (word.y > getSize().height * 5 / 6)
			{
				FallingWords.lost(score);
				timer.stop();
			}
		}

		if (Math.random() < DROP_PROBABILITY)
		{
			counter++;
			if (counter % LEVEL_CHANGE == 0)
				difficulty++;
			if (difficulty <= FallingWords.DEATH)
			{
				int randomX = (int)(Math.random() * (getSize().width - 80));
				words.add(new MyString(FallingWords.randomWord(difficulty), randomX, 0, difficulty));
			}
		}
	}
}

final class Scoreboard extends JPanel
{
	static final int MAX_PEOPLE = 10;

	private final String filename;
	private TreeSet<PeopleInfo> infos;

	Scoreboard(String filename)
	{
		this.filename = filename;
		this.infos  = new TreeSet<PeopleInfo>();

		try
		{
			Scanner scanner = new Scanner(new File(filename));

			while (scanner.hasNext())
			{
				infos.add(new PeopleInfo(scanner.nextInt(), scanner.nextLine().substring(1)));
			}
		}
		catch (IOException e)
		{
			System.out.println("SOMEBODY MESSED UP THE FILES! >:(");
		}
	}

	boolean makesList(int score)
	{
		return infos.size() < MAX_PEOPLE || score > infos.last().score;
	}

	void addScore(String name, int score)
	{
		if (name == null) return;

		PeopleInfo temp = new PeopleInfo(score, name);
		if (!contains(temp))
			infos.add(temp);
		if (contains(temp) && getScore(name) < score)
		{
			remove(temp);
			infos.add(temp);
		}

		if (infos.size() > MAX_PEOPLE)
			infos.remove(infos.size() - 1);
	}

	private boolean contains(PeopleInfo info_)
	{
		for (PeopleInfo info : infos)
			if (info.name.equalsIgnoreCase(info_.name))
				return true;

		return false;
	}

	private void remove(PeopleInfo info_)
	{
		for (PeopleInfo info : infos)
			if (info.name.equalsIgnoreCase(info_.name))
			{
				infos.remove(info);
				return;
			}
	}

	private int getScore(String name)
	{
		for (PeopleInfo info : infos)
			if (info.name.equalsIgnoreCase(name))
				return info.score;

		return -1;
	}

	void write()
	{
		try
		{
			PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

			for (PeopleInfo info : infos)
				output.println(info.score + " " + info.name);

			output.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman", 0, 48));
		g.drawString("HIGH SCORES!", getSize().width / 2 - 160, 100);
		g.drawLine(0, 150, getSize().width, 150);

		g.setFont(new Font("Times New Roman", 0, 24));
		Iterator<PeopleInfo> iterator = infos.iterator();
		for (int i = 0; i < infos.size(); i++)
		{
			PeopleInfo temp = iterator.next();
			g.drawString(temp.name, 50, 200 + 30 * i);
			g.drawString("" + temp.score, getSize().width - 100, 200 + 30 * i);
		}
	}

	class PeopleInfo implements Comparable<PeopleInfo>
	{
		final String name;
		final int score;

		PeopleInfo(int score, String name)
		{
			this.name = name;
			this.score = score;
		}

		public boolean equals(Object other)
		{
			return name.equalsIgnoreCase(((PeopleInfo)other).name);
		}

		public int compareTo(PeopleInfo other)
		{
			return other.score - score;
		}
	}
}
