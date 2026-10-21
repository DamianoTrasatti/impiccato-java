import java.util.Arrays;

public class Giocatore {
	private String parolaDaIndovinare;
	private char[] parolaClientAttuale;
	private int vite;
	private String[] paroleInviate;
	private char[] lettereInviateCorrette;
	private char[] lettereInviateSbagliate;

	public Giocatore(String parola) {
		parolaDaIndovinare = parola;
		parolaClientAttuale = new char[parola.length()];
		for (int i = 0; i < parolaClientAttuale.length; i++) {
			parolaClientAttuale[i] = '_';
		}
		vite = 6;
		paroleInviate = new String[0];
		lettereInviateCorrette = new char[0];
		lettereInviateSbagliate = new char[0];
	}

	// ---- get ----
	public String getParolaDaIndovinare() {
		return parolaDaIndovinare;
	}

	public char[] getParolaClientAttuale() {
		return parolaClientAttuale;
	}

	public int getVite() {
		return vite;
	}

	public String[] getParoleInviate() {
		return paroleInviate;
	}

	public char[] getLettereInviateCorrette() {
		return lettereInviateCorrette;
	}

	public char[] getLettereInviateSbagliate() {
		return lettereInviateSbagliate;
	}

	// ---- set ----
	public void setParolaDaIndovinare(String parolaDaIndovinare) {
		this.parolaDaIndovinare = parolaDaIndovinare;
	}

	public void setParolaClientAttuale(char[] parolaClientAttuale) {
		this.parolaClientAttuale = parolaClientAttuale;
	}

	public void setVite(int vite) {
		this.vite = vite;
	}

	public void setParoleInviate(String[] paroleInviate) {
		this.paroleInviate = paroleInviate;
	}

	// ---- metodi per aggiungere elementi agli array ----
	public void aggiungiLetteraCorretta(char c) {
		lettereInviateCorrette = Arrays.copyOf(lettereInviateCorrette, lettereInviateCorrette.length + 1); // cosa copiare e nuova dimensione
		lettereInviateCorrette[lettereInviateCorrette.length - 1] = c;
		Arrays.sort(lettereInviateCorrette);
	}

	public void aggiungiLetteraSbagliata(char c) {
		lettereInviateSbagliate = Arrays.copyOf(lettereInviateSbagliate, lettereInviateSbagliate.length + 1);
		lettereInviateSbagliate[lettereInviateSbagliate.length - 1] = c;
		Arrays.sort(lettereInviateSbagliate);
	}

	public void aggiungiParola(String parola) {
		paroleInviate = Arrays.copyOf(paroleInviate, paroleInviate.length + 1);
		paroleInviate[paroleInviate.length - 1] = parola;
		Arrays.sort(paroleInviate);
	}

	// true se la lettera e' gia' stata inviata (giusta o sbagliata)
	public boolean contieneLettera(char c) {
		for (int i = 0; i < lettereInviateCorrette.length; i++) {
			if (lettereInviateCorrette[i] == c) {
				return true;
			}
		}
		for (int i = 0; i < lettereInviateSbagliate.length; i++) {
			if (lettereInviateSbagliate[i] == c) {
				return true;
			}
		}
		return false;
	}

	// true se la parola e' gia' stata inviata
	public boolean contieneParola(String parola) {
		for (int i = 0; i < paroleInviate.length; i++) {
			if (paroleInviate[i].equals(parola)) { // equals perche == confronta gli indirizzi in memoria
				return true;
			}
		}
		return false;
	}
}