import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Main {
	private static int totalInstrucoes = 0; // fazer os calculos
	private static int totalTroca = 0 ; // fazer o calculos
	private static int quantum;
	private static int creditosTotal = 0; // variavel para controlar redistribuição
	private static ArrayList<Processo> processosAtivos; // Lista Processo ativos
	private static ArrayList<Processo> processosBloqueados;// Lista Prcesso bloqueados
	private static ArrayList<String> logFile;// receber as respostas para logfile
	private static String dir = System.getProperty("user.dir");

	public static void main(String[] args) throws IOException {
		dir = dir.substring(0, dir.length() - 4) + "/";
		
		processosBloqueados = new ArrayList<>();
		logFile = new ArrayList<>();
		inicializarEscalonador();
		redestribuir();

		Collections.sort(processosAtivos);

		// Escrita LogFile
		for (Processo p : processosAtivos) {
			logFile.add("Carregando " + p.getNome());
		}

		// Enquanto as listas nao tiverem vazias, executa o escalonador  
 		while (processosAtivos.size() > 0  || processosBloqueados.size() > 0) {
			if (creditosTotal == 0) {
				redestribuir();
			}
			
			Collections.sort(processosAtivos);

			if (processosAtivos.size() > 0) {
				executar(0);
			}
			
			else if (processosBloqueados.size() > 0) {
				descBloq();	
			}
	    }
 		
 		//Escrita no logFile
 		logFile.add("Média de Trocas " + (double) totalTroca / 10);
 		logFile.add("Média de Instrucoes " + (double) totalInstrucoes / totalTroca);
 		logFile.add("Quantum " + quantum);
 		
 		escreverLogFile();
	}
	  
	public static void executar(int index) {
		creditosTotal--;
		totalTroca += 1;
		int numInstrucoes = 0;

		//escrita no logfile
		logFile.add("Executando " + processosAtivos.get(index).getNome());
		processosAtivos.get(index).setCreditos(processosAtivos.get(index).getCreditos() - 1);
		
		//diminui todos da lista de bloq
		descBloq();
		
		//até no maximo tamanho do quantum
		for (int i = 0; i < quantum; i++) {
			numInstrucoes += 1;
			
			//le o comando, deixa pronto para o próximo
			String comando = processosAtivos.get(index).getInstrucoes().get(processosAtivos.get(index).getIndex());
			processosAtivos.get(index).setIndex(processosAtivos.get(index).getIndex() + 1);
			
			//identifica o tipo de comando
			switch (comando) {
				//comando comum
				case "COM":
					break;

				//adiciona na lista de bloqueados e interrompe a execução
				case "E/S":
					Processo processo1 = processosAtivos.get(index);
					processo1.setBloq(2); // esperar 2 quantum
					processosBloqueados.add(processo1);
					totalInstrucoes += numInstrucoes;

					logFile.add("E/S iniciada em " + processosAtivos.get(index).getNome());

					if (numInstrucoes - 1 == 0) {
						logFile.add("Interrompendo " + processosAtivos.get(index).getNome() + " apos " + (numInstrucoes) + " intrucao (havia apenas a E/S)");
					} else {
						logFile.add("Interrompendo " + processosAtivos.get(index).getNome()  + " apos " + (numInstrucoes) + " intrucao(oes) (E/S)");
					}

					processosAtivos.remove(index);

					return;
				
				// Remove da lista de ativos, processo finalizado
				case "SAIDA":
					logFile.add(processosAtivos.get(index).getNome() + " terminado, " + " X=" + processosAtivos.get(index).getX() + " Y=" + processosAtivos.get(index).getY());	
					processosAtivos.remove(index);
					totalInstrucoes += numInstrucoes;
					return;

				//Atribui os valores das variaveis x e y
				default:
					String result[] = new String[2];
					result = comando.split("=");
					if (comando.contains("X=")) {
						processosAtivos.get(index).setX(Integer.parseInt(result[1]));	
					} else {
						processosAtivos.get(index).setY(Integer.parseInt(result[1]));
					}
			}
		}

		totalInstrucoes += numInstrucoes;
		logFile.add("interrompendo " + processosAtivos.get(index).getNome() + " após " + numInstrucoes + " instruções");
	}

	public static void inicializarEscalonador() throws FileNotFoundException, IOException {
		FileReader fileR;
		BufferedReader buff;
		BufferedReader buff2;
		
		fileR = new FileReader(dir + "processos/prioridades.txt");
		buff2 = new BufferedReader(fileR);
		String file;

		// leitura dos processos
		processosAtivos = new ArrayList<>();
		
		for (int i = 1; i <= 10; i++) {
			Processo processo = new Processo();

			fileR = (i != 10) ? new FileReader(dir + "processos/0" + i + ".txt") : new FileReader(dir + "processos/10.txt");
			
			buff = new BufferedReader(fileR);
			processo.setNome(buff.readLine());
			processo.setPrioridade(Integer.parseInt(buff2.readLine()));

			// guardando instruções
			ArrayList<String> instrucoes = new ArrayList<>();
			String instrucao = buff.readLine();
			instrucoes.add(instrucao);

			while (!instrucao.equalsIgnoreCase("Saida")) {
				instrucao = buff.readLine();
				instrucoes.add(instrucao);
			}

			processo.setInstrucoes(instrucoes);
			processosAtivos.add(processo);
		}
		
		//Ler o quantum
		fileR = new FileReader(dir + "processos/quantum.txt");
		buff2 = new BufferedReader(fileR);
		quantum = Integer.parseInt(buff2.readLine());

	}

	public static void descBloq() {
		//metódo para decrementar o bloq
		ArrayList<Processo> processosBloqueados1 = new ArrayList<>();

		for (Processo p : processosBloqueados) {
			p.setBloq(p.getBloq() - 1 );

			if (p.getBloq() == 0) {
				processosAtivos.add(p);
			} else {
				processosBloqueados1.add(p);
			}
		}

		processosBloqueados = processosBloqueados1;
	}
		
	public static void redestribuir() {
		// metodo para redistribuir os creditos
		creditosTotal = 0;

		for (Processo p : processosAtivos) {
			creditosTotal += p.getPrioridade();
			p.setCreditos(p.getPrioridade());
		}
		
		for (Processo p : processosBloqueados) {
			creditosTotal += p.getPrioridade();
			p.setCreditos(p.getPrioridade());
		}
	}

	public static void escreverLogFile() throws IOException {
		//método de escrita do log file
		FileWriter arq = new FileWriter(dir + "logfile/log" + quantum +".txt");
		BufferedWriter gravarArq = new BufferedWriter(arq);
		
		for (String s : logFile) {
			arq.write(s + "\n");
		}

		arq.close();
	}
}
