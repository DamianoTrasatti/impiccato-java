//SERVER
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerStr {
	ServerSocket server = null;
	Socket client = null;
	String stringaRicevuta = null;

	BufferedReader inDalClient;
	DataOutputStream outVersoClient;

	ArrayList<Giocatore> giocatori = new ArrayList<Giocatore>();
	Random random = new Random();
	String messaggio = ""; // messaggio da mandare al client

	String[] parole = { "COMPUTER", "CARTELLA", "TASTIERA", "PROCESSORE", "MOUSE", "PROGRAMMA", "MONITOR",
			"PASSWORD", "STAMPANTE", "INTERNET", "BROWSER", "SCHERMO", "SERVER", "TABLET", "CLIENT", "SMARTPHONE",
			"RETE", "PORTATILE", "ROUTER", "SOFTWARE", "DATABASE", "HARDWARE", "TABELLA", "CODICE", "PIXEL",
			"PYTHON", "VIDEO", "JAVA", "AUDIO", "FLUTTER", "DOWNLOAD", "FILE", "UPLOAD", "FOLDER", "VIRUS", "GIOCO",
			"ANTIVIRUS", "APP", "ALGORITMO" };

	// ---------- PARTE SOCKET ----------

	// crea il ServerSocket una sola volta (non lo chiudiamo dopo l'accept)
	public void avviaServer() {
		try {
			System.out.println("1 SERVER partito in esecuzione...");
			server = new ServerSocket(6789);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante l'istanza del server");
			System.exit(1);
		}
	}

	// accetta un client e crea subito il suo Giocatore
	public Socket attendi() {
		try {
			System.out.println("In attesa di un client...");
			client = server.accept();
			inDalClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outVersoClient = new DataOutputStream(client.getOutputStream());
			creaGiocatore();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante l'accettazione del client");
			System.exit(1);
		}
		return client;
	}

	// ---------- PARTE GIOCO ----------

	public void creaGiocatore() {
		int indice = random.nextInt(parole.length); // da 0 a 38
		giocatori.add(new Giocatore(parole[indice]));
	}

	public Giocatore ultimoGiocatore() {
		return giocatori.get(giocatori.size() - 1);
	}

	public void comunica() {
		try {
			System.out.println("3...client connesso, inizia la partita");
			inviaStato("INIZIO", "Nuova partita, indovina la parola!");
			boolean continua = true;
			while (continua) {
				Giocatore g = ultimoGiocatore();
				stringaRicevuta = inDalClient.readLine();
				if (stringaRicevuta == null) {
					System.out.println("Il client si è disconnesso");
					break; // il client si e' disconnesso
				}
				stringaRicevuta = stringaRicevuta.toUpperCase();
				System.out.println("6...ricevuto dal client: " + stringaRicevuta);

				String esito = elabora(g, stringaRicevuta);
				inviaStato(esito, messaggio);

				if (esito.equals("VINTO") || esito.equals("PERSO")) {
					// partita finita: il client risponde SI o NO alla rigiocata
					String risposta = inDalClient.readLine();
					if (risposta != null && risposta.toUpperCase().equals("SI")) {
						creaGiocatore();
						inviaStato("INIZIO", "Nuova partita, indovina la parola!");
					} else {
						continua = false;
					}
				}
			}
			System.out.println("9...SERVER chiude la connessione con il client");
			client.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Errore durante la comunicazione col client");
			try {
				client.close();
			} catch (Exception e2) {
			}
		}
	}

	// decide se e' una lettera o una parola e restituisce l'esito
	public String elabora(Giocatore g, String msg) {
		if (msg.length() == 1) {
			if (!Character.isLetter(msg.charAt(0))) {
				messaggio = "Il carattere inviato non e' una lettera, rimandalo!";
				return "ERRORE";
			}
			return elaboraLettera(g, msg.charAt(0));
		} else if (msg.length() == g.getParolaDaIndovinare().length()) {
			return elaboraParola(g, msg);
		} else {
			messaggio = "Quanto inserito non e' corretto, rimandalo!";
			return "ERRORE";
		}
	}

	public String elaboraLettera(Giocatore g, char c) {
		if (g.contieneLettera(c)) {
			messaggio = "La lettera inviata era gia' stata inviata, inviane un'altra!";
			return "ERRORE";
		}

		char[] parola = g.getParolaDaIndovinare().toCharArray();
		char[] attuale = g.getParolaClientAttuale();
		boolean corretta = false;

		for (int i=0; i<parola.length; i++){
			if (parola[i] == c){
				corretta = true;
				attuale[i] = c;
			}
		}

		// btw i set sono inutili perche in java gli array non vengono copiati ma gli diamo il puntatore alla memoria
		// quindi modificando attuale modifichiamoa anche quello che è dentro giocatore

		if (!corretta) {
			g.setVite(g.getVite() - 1);
			g.aggiungiLetteraSbagliata(c);
			if (controllaVite(g)) {
				return "PERSO";
			}
		} else {
			g.aggiungiLetteraCorretta(c);
			g.setParolaClientAttuale(attuale);
			if (String.valueOf(attuale).equals(g.getParolaDaIndovinare())) {
				messaggio = "Parola indovinata, complimenti!";
				return "VINTO";
			}
		}

		String esitoLettera;
		if (corretta) {
			esitoLettera = "corretta";
		} else {
			esitoLettera = "sbagliata";
		}
		messaggio = "La lettera: " + c + " e' " + esitoLettera + ", vite attuali: " + g.getVite()
				+ ", stato attuale della parola: " + String.valueOf(g.getParolaClientAttuale())
				+ ", lettere corrette inviate fino ad ora: " + String.valueOf(g.getLettereInviateCorrette())
				+ ", lettere sbagliate inviate fino ad ora: " + String.valueOf(g.getLettereInviateSbagliate());
		return "OK";
	}

	public String elaboraParola(Giocatore g, String parola) {
		if (g.contieneParola(parola)) {
			messaggio = "Parola gia' inviata, mandane un'altra!";
			return "ERRORE";
		}
		char[] tentativo = parola.toCharArray();
		char[] giusta = g.getParolaDaIndovinare().toCharArray();
		boolean uguali = true;
		for (int i = 0; i < giusta.length; i++) {
			if (tentativo[i] != giusta[i]) {
				uguali = false;
			}
		}
		if (uguali) {
			g.setParolaClientAttuale(giusta);
			messaggio = "Parola indovinata, complimenti!";
			return "VINTO";
		}
		g.aggiungiParola(parola);
		g.setVite(g.getVite() - 1);
		if (controllaVite(g)) {
			return "PERSO";
		}
		messaggio = "La parola: " + parola + " e' sbagliata, vite attuali: " + g.getVite();
		return "OK";
	}

	// controlla se le vite sono a 0 e prepara il messaggio di sconfitta
	public boolean controllaVite(Giocatore g) {
		if (g.getVite() == 0) {
			messaggio = "Zero vite rimaste, hai perso! La parola era: " + g.getParolaDaIndovinare();
			return true;
		}
		return false;
	}

	// formato: ESITO|parolaClient|vite|corrette|sbagliate|paroleSbagliate|messaggio
	public void inviaStato(String esito, String msg) throws IOException {
		Giocatore g = ultimoGiocatore();
		String paroleSbagliate = "";
		String[] inviate = g.getParoleInviate();
		for (int i = 0; i < inviate.length; i++) {
			if (i > 0) {
				paroleSbagliate = paroleSbagliate + ",";
			}
			paroleSbagliate = paroleSbagliate + inviate[i];
		}
		String risposta = esito + "|" + String.valueOf(g.getParolaClientAttuale()) + "|" + g.getVite() + "|"
				+ String.valueOf(g.getLettereInviateCorrette()) + "|"
				+ String.valueOf(g.getLettereInviateSbagliate()) + "|" + paroleSbagliate + "|" + msg;
		System.out.println("7...invio al client: " + risposta);
		outVersoClient.writeBytes(risposta + '\n');
	}

	public static void main(String[] args) {
		ServerStr servente = new ServerStr();
		servente.avviaServer();
		while (true) {
			servente.attendi();
			servente.comunica();
		}
	}
}
