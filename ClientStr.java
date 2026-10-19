
// CLIENT
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ClientStr extends JPanel {
	String nomeServer = "localhost";
	int portaServer = 6789;
	Socket miosocket;
	String stringaRicevutaDalServer;
	DataOutputStream outVersoServer;
	BufferedReader inDalServer;

	// dati ricevuti dal server: il client li mostra soltanto
	String esito = "";
	String parolaAttuale = "";
	int vite = 6;
	String lettereCorrette = "";
	String lettereSbagliate = "";
	String paroleSbagliate = "";
	String messaggio = "";

	// componenti del pannello
	JTextField campoLettera;
	JButton bottoneLettera;
	JTextField[] campiParola = new JTextField[0];
	JButton bottoneParola;
	JLabel labelMessaggio;

	public ClientStr() {
		setLayout(null);
		setPreferredSize(new Dimension(700, 640));

		labelMessaggio = new JLabel("");
		labelMessaggio.setBounds(20, 300, 660, 60);
		add(labelMessaggio);

		campoLettera = new JTextField();
		campoLettera.setBounds(20, 390, 40, 30);
		add(campoLettera);

		bottoneLettera = new JButton("Invia lettera");
		bottoneLettera.setBounds(70, 390, 130, 30);
		bottoneLettera.addActionListener(new ActionListener() {
			// Override the actionPerformed() method
			public void actionPerformed(ActionEvent e) {
				String lettera = campoLettera.getText().trim();
				if (lettera.length() != 1) {
					labelMessaggio.setText("Inserisci una sola lettera!");
					return;
				}
				inviaAlServer(lettera);
			}
		});
		add(bottoneLettera);

		bottoneParola = new JButton("Invia parola");
		bottoneParola.setBounds(360, 430, 130, 30);
		bottoneParola.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String parola = "";
				for (int i = 0; i < campiParola.length; i++) {
					parola = parola + campiParola[i].getText().trim();
				}
				if (parola.length() != campiParola.length) {
					labelMessaggio.setText("Compila tutte le caselle della parola!");
					return;
				}
				inviaAlServer(parola);
			}
		});
		add(bottoneParola);
	}

	public Socket connetti() {
		System.out.println("2 CLIENT partito in esecuzione");
		try {
			miosocket = new Socket(nomeServer, portaServer);
			outVersoServer = new DataOutputStream(miosocket.getOutputStream());
			inDalServer = new BufferedReader(new InputStreamReader(miosocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("HOST sconosciuto");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la connessione");
			System.exit(1);
		}
		return miosocket;
	}

	// riceve il primo messaggio del server e apre la finestra
	public void comunica() {
		try {
			riceviStato();
			JFrame finestra = new JFrame("Impiccato");
			finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			finestra.add(this);
			finestra.pack();
			finestra.setVisible(true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la comunicazione col server");
			System.exit(1);
		}
	}

	// formato:
	// ESITO-parolaClient-vite-lettereCorrette-lettereSbagliate-paroleSbagliate-messaggio
	public void riceviStato() {
		try {
			stringaRicevutaDalServer = inDalServer.readLine();
			System.out.println("in dal server");
			System.out.println(stringaRicevutaDalServer);
			String[] p = stringaRicevutaDalServer.split("-");
			esito = p[0];
			parolaAttuale = p[1];
			vite = Integer.parseInt(p[2]);
			lettereCorrette = p[3];
			lettereSbagliate = p[4];
			paroleSbagliate = p[5];
			messaggio = p[6];
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la comunicazione col server");
			System.exit(1);
		}

		if (esito.equals("INIZIO")) {
			creaCampiParola();
		}
		campoLettera.setText("");
		for (int i = 0; i < campiParola.length; i++) {
			campiParola[i].setText("");
		}
		labelMessaggio.setText("<html>" + messaggio + "</html>");
		repaint();
	}

	// un campo per ogni lettera: la lunghezza si ricava dalla parola con i trattini
	public void creaCampiParola() {
		for (int i = 0; i < campiParola.length; i++) {
			remove(campiParola[i]); // devo rimuovere quelli vecchi, nel caso in cui si rigioca
		}
		campiParola = new JTextField[parolaAttuale.length()];
		for (int i = 0; i < campiParola.length; i++) {
			campiParola[i] = new JTextField();
			campiParola[i].setBounds(360 + i * 30, 390, 25, 30);
			add(campiParola[i]);
		}
	}

	// invio la parola/lettera -> vedo se ho vinto/perso -> se si faccio quello che devo fare per i vari casi
	public void inviaAlServer(String testo) {
		try {
			outVersoServer.writeBytes(testo.toUpperCase() + '\n');
			System.out.println("inviamo al server:");
			System.out.println(testo.toUpperCase());
			riceviStato();
			if (esito.equals("VINTO") || esito.equals("PERSO")) {
				chiediRigioca();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la comunicazione col server");
			System.exit(1);
		}
	}

	public void chiediRigioca() {
		int scelta = JOptionPane.showConfirmDialog(this, messaggio + "\nVuoi rigiocare?", "Fine partita",
				JOptionPane.YES_NO_OPTION);
		try {
			if (scelta == JOptionPane.YES_OPTION) {
				outVersoServer.writeBytes("SI" + '\n');
				riceviStato(); // arriva il messaggio INIZIO della nuova partita
			} else {
				outVersoServer.writeBytes("NO" + '\n');
				miosocket.close();
				System.exit(0);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la comunicazione col server");
			System.exit(1);
		}
	}

	// ---------- DISEGNO ----------
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);

		// in alto a sinistra: parola con i trattini; in alto a destra: vite
		g.setFont(new Font("Monospaced", Font.BOLD, 28));
		String spaziata = "";
		for (int i = 0; i < parolaAttuale.length(); i++) {
			spaziata = spaziata + parolaAttuale.charAt(i) + " ";
		}
		g.drawString(spaziata, 20, 40);
		g.drawString("Vite: " + vite, 540, 40);

		// forca
		g.drawLine(250, 290, 450, 290);
		g.drawLine(300, 290, 300, 70);
		g.drawLine(300, 70, 350, 70);
		g.drawLine(350, 70, 350, 100);

		// omino: un pezzo per ogni errore
		int errori = 6 - vite;
		if (errori >= 1) {
			g.drawOval(330, 100, 40, 40); // testa
		}
		if (errori >= 2) {
			g.drawLine(350, 140, 350, 210); // corpo
		}
		if (errori >= 3) {
			g.drawLine(350, 210, 320, 240); // gamba sinistra
		}
		if (errori >= 4) {
			g.drawLine(350, 210, 380, 240); // gamba destra
		}
		if (errori >= 5) {
			g.drawLine(350, 160, 320, 130); // braccio sinistro
		}
		if (errori >= 6) {
			g.drawLine(350, 160, 380, 130); // braccio destro
		}

		g.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g.drawString("Lettera:", 20, 380);
		g.drawString("Parola intera:", 360, 380);

		// lettere sbagliate (rosso) poi lettereCorrette (verde)
		for (int i = 0; i < lettereSbagliate.length(); i++) {
			g.setColor(Color.RED);
			g.drawRect(20 + i * 30, 450, 25, 25);
			g.setColor(Color.BLACK);
			g.drawString(String.valueOf(lettereSbagliate.charAt(i)), 28 + i * 30, 468);
		}
		for (int i = 0; i < lettereCorrette.length(); i++) {
			g.setColor(Color.GREEN);
			g.drawRect(20 + i * 30, 490, 25, 25);
			g.setColor(Color.BLACK);
			g.drawString(String.valueOf(lettereCorrette.charAt(i)), 28 + i * 30, 508);
		}

		// parole sbagliate
		g.drawString("Parole sbagliate:", 360, 490);
		if (paroleSbagliate.length() > 0) {
			String[] parole = paroleSbagliate.split(",");
			for (int i = 0; i < parole.length; i++) {
				g.drawString(parole[i], 360, 510 + i * 18);
			}
		}
	}

	public static void main(String[] args) {
		ClientStr cliente = new ClientStr();
		cliente.connetti();
		cliente.comunica();
	}
}
