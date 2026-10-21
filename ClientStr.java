
// CLIENT
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

// trim: toglie gli spazi a inizio a fine
/*
	JFrame: la finestra vera e propria con titolo, barra ed x per chiudere
	JPanel: superficie rettangolare che si mette dentro la finestra, ci si mettono componenti e disegni come linee
*/

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
	JFrame finestra;
	JTextField campoLettera;
	JButton bottoneLettera;
	JTextField[] campiParola = new JTextField[0];
	JButton bottoneParola;
	JLabel labelMessaggio;

	// ux
	Timer timer;
	int secondiRimasti = 60;
	JLabel labelTimer;

	public ClientStr() {
		setLayout(null); // decidiamo noi dove posizionare gli elementi
		setPreferredSize(new Dimension(700, 640)); // dimensione del pannello in pixel

		labelTimer = new JLabel("Tempo: 60 s");
		labelTimer.setBounds(540, 50, 140, 25); // sotto le vite
		add(labelTimer);

		timer = new Timer(1000, new ActionListener() { // scatta ogni secondo
			public void actionPerformed(ActionEvent e) {
				secondiRimasti--;
				labelTimer.setText("Tempo: " + secondiRimasti + " s");
				if (secondiRimasti <= 10) {
					labelTimer.setForeground(Color.RED); // ultimi 10 secondi, setforeground cambia le proprietà del componente
				}
				if (secondiRimasti == 0) {
					timer.stop();
					tempoScaduto();
				}
			}
		});

		labelMessaggio = new JLabel("");
		labelMessaggio.setBounds(20, 300, 660, 60); // x, y, width, height
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
				// compongo la parola
				for (int i = 0; i < campiParola.length; i++) {
					parola = parola + campiParola[i].getText().trim();
				}
				// controllo che non ci siano campi vuoti
				for (int i = 0; i < campiParola.length; i++) {
					if (campiParola[i].getText().trim().equals("")) {
						labelMessaggio.setText("Tutti i campi della parola devono essere inseriti");
						return;
					}
				}
				// controllo che la lunghezza coincida
				if (parola.length() != campiParola.length) {
					labelMessaggio.setText("Compila tutte le caselle della parola in modo corretto!");
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
			finestra = new JFrame("Impiccato");
			finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			finestra.add(this);
			finestra.pack(); // adatta la finestra al pannello
			finestra.setVisible(true);
			timer.start();
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
		labelMessaggio.setText("<html>" + messaggio + "</html>"); // html cosi che va a capo in automatico
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

	// invio la parola/lettera -> vedo se ho vinto/perso -> se si faccio quello che
	// devo fare per i vari casi
	public void inviaAlServer(String testo) {
		try {
			outVersoServer.writeBytes(testo.toUpperCase() + '\n');
			System.out.println("inviamo al server:");
			System.out.println(testo.toUpperCase());
			riceviStato();
			if (esito.equals("VINTO") || esito.equals("PERSO")) {
				timer.stop();
				chiediRigioca();
			} else {
				restartTimer();
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
			miosocket.close();
		} catch (Exception e) {
			System.out.println("Errore durante la chiusura");
		}
		if (scelta == JOptionPane.YES_OPTION) {
			ClientStr nuovo = new ClientStr();
			nuovo.connetti();
			nuovo.comunica(); // aspetta il suo turno, poi apre la nuova finestra
			finestra.dispose(); // chiude la finestra vecchia
		} else {
			System.exit(0);
		}
	}

	public void tempoScaduto() {
		JOptionPane.showMessageDialog(this, "Tempo scaduto! Il client si chiude.");
		try {
			miosocket.close();
		} catch (Exception e) {
			System.out.println("Errore durante la chiusura");
		}
		System.exit(0);
	}


	public void restartTimer(){
		secondiRimasti = 60;
		labelTimer.setText("Tempo: 60 s");
		labelTimer.setForeground(Color.BLACK); // cambia le proprieta del componente
		timer.restart();
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
		if (paroleSbagliate.length() > 0) {
			String[] parole = paroleSbagliate.split(",");
			for (int i = 0; i < parole.length; i++) {
				g.setColor(Color.RED);
				g.drawRect(360, 490 + i * 25, 12 * parole[i].length() + 10, 22);
				g.setColor(Color.BLACK);
				g.drawString(parole[i], 365, 507 + i * 25);
			}
		}
	}

	public static void main(String[] args) {
		ClientStr cliente = new ClientStr();
		cliente.connetti();
		cliente.comunica();
	}
}