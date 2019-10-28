import static java.lang.String.format;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
	private static int totalInstrucoes = 0; // fazer os calculos
	private static int totalTroca = 0 ; // fazer o calculos
	private static int QUANTUM_MAXIMO;
	private static int CREDITOS_TOTAL = 0; // variavel para controlar redistribuição
	private static ArrayList<Processo> processosAtivos; // Lista Processo ativos
	private static ArrayList<Processo> processosBloqueados;// Lista Prcesso bloqueados
	private static List<List<Processo>> multiplaListaDePrioridade = new ArrayList<>();
	private static ArrayList<String> logFile;// receber as respostas para logfile
	private static String dir = System.getProperty("user.dir");
	private static Integer PRIORIDADE_MAX;

	public static void main(String[] args) throws IOException {
//		dir = dir.substring(0, dir.length() - 4) + "/";
		dir += "/";
		System.out.println(dir);
		processosBloqueados = new ArrayList<>();
		logFile = new ArrayList<>();
		inicializarEscalonador();

		Collections.sort(processosAtivos);

		EscritaLogFile();

		while ((processosAtivos.size() > 0) || (processosBloqueados.size() > 0)){
			redestribuir();
			setPrioridadeMax();
			inicializaFilaDeMultiplaPrioridade();
			correTodosOsCreditos();

				}


		logFile.add("Média de Trocas " + (double) totalTroca / 10);
		logFile.add("Média de Instrucoes " + (double) totalInstrucoes / totalTroca);
		logFile.add("Quantum " + QUANTUM_MAXIMO);

		escreverLogFile();

		// Enquanto as listas nao tiverem vazias, executa o escalonador  
// 		while (processosAtivos.size() > 0  || processosBloqueados.size() > 0) {
//			if (creditosTotal == 0) {
//				redestribuir();
//			}
//
//			Collections.sort(processosAtivos);
//
//			if (processosAtivos.size() > 0) {
//				executar(0);
//			}
//
//			else if (processosBloqueados.size() > 0) {
//				descBloq();
//			}
//	    }
//
// 		//Escrita no logFile
// 		logFile.add("Média de Trocas " + (double) totalTroca / 10);
// 		logFile.add("Média de Instrucoes " + (double) totalInstrucoes / totalTroca);
// 		logFile.add("Quantum " + QUANTUM_MAXIMO);
//
	}

	private static void correTodosOsCreditos() {
		for(int prioridade = 0; prioridade <= PRIORIDADE_MAX; prioridade++) {
			List<Processo> listaPrioridade = multiplaListaDePrioridade.get(prioridade);

			while (!listaPrioridade.isEmpty()) {
				Processo processo = listaPrioridade.get(0);

				for(int i=0;i <= listaPrioridade.size();i++){
					if(aListaInteiraEstaBlocada(listaPrioridade, i)){
						String filaPrioridadeNumber = processo.getCreditos().toString();
						logFile.add(format("TODOS OS PROCESSOS BLOQUEADOS NA FILA %s E NA TROCA Nº %s, ESPERANDO UM QUANTUM",filaPrioridadeNumber.toUpperCase(),totalTroca));
						descBloq();
						//fluxo de todos bloqueados
					}else if(ehUmProcessoLivre(processo)){
						totalTroca += 1;
						executa(processo);
						listaPrioridade.remove(processo);

						if(!isLastList(prioridade))
							mudaDeFila(processo);
					break;
					}else{
						//pega proximo processo
						processo = listaPrioridade.get(i);
					}
				}
			}
		}
	}

	private static boolean ehUmProcessoLivre(Processo processo) {
		return processosAtivos.contains(processo);
	}

	private static boolean aListaInteiraEstaBlocada(List<Processo> listaPrioridade, int i) {
		return i == listaPrioridade.size();
	}

	private static boolean isLastList(int prioridade) {
		return (prioridade == PRIORIDADE_MAX);
	}

	private static void setPrioridadeMax() {
		PRIORIDADE_MAX = processosAtivos.stream()
				.map(Processo::getCreditos)
				.max(Integer::compareTo).get();
	}

	private static void inicializaFilaDeMultiplaPrioridade() {
		List<List<Processo>> novaListaDePrioridade = new ArrayList<>();
		for (Integer i = PRIORIDADE_MAX; i >= 0; i--) {
			final Integer prioridade = i;

			List<Processo> processosDeMesmaPrioridade = processosAtivos.stream()
					.filter(processo -> prioridade.equals(processo.getCreditos()))
					.collect(Collectors.toList());

			for(Processo p : processosBloqueados){
			if(prioridade.equals(p.getCreditos())){
				processosDeMesmaPrioridade.add(p);
			}
			}

			novaListaDePrioridade.add(processosDeMesmaPrioridade);
		}
		multiplaListaDePrioridade = novaListaDePrioridade;
	}

	private static void executa(Processo processo) {
		aumentaQuantum(processo);

		reduzCreditos(processo);
		for(int instrucoesExecutadas = 0;instrucoesExecutadas < processo.getQuantum();){
			totalInstrucoes++;
			instrucoesExecutadas++;
			descBloq();

			//escrita no logfile
			logFile.add("Executando " + processo.getNome());

			String instrucao = processo.getInstrucao();
			processo.incProgramCounter();

			switch(instrucao){
				case("COM"):
					break;

				case("E/S"):
					logFile.add("E/S iniciada em " + processo.getNome());
					logAmountOfInstructionsOnIO(processo, instrucoesExecutadas);

					processo.setBloq(2); // esperar 2 quantum
					processosBloqueados.add(processo);
					processosAtivos.remove(processo);
					return;

				case("SAIDA"):
					logFile.add(processo.getNome() + " terminado, " + " X=" + processo.getRegistradorX() + " Y=" + processo.getRegistradorY());

					processosAtivos.remove(processo);
					return;

				default:
					String result[] = new String[2];
					result = instrucao.split("=");
					if (instrucao.contains("X=")) {
						processo.setRegistradorX(Integer.parseInt(result[1]));
					} else {
						processo.setRegistradorY(Integer.parseInt(result[1]));
					}
			}

			//só log ignorar
			if(instrucoesExecutadas == processo.getQuantum())
				logFile.add("interrompendo " + processo.getNome() + " após " + instrucoesExecutadas + " instruções");

		}
	}

	private static void logAmountOfInstructionsOnIO(Processo processo, int instrucoesExecutadas) {
		if (instrucoesExecutadas == 1) {
			logFile.add("Interrompendo " + processo.getNome() + " apos " + (instrucoesExecutadas) + " intrucao (havia apenas a E/S)");
		} else {
			logFile.add("Interrompendo " + processo.getNome()  + " apos " + (instrucoesExecutadas) + " intrucao(oes) (E/S)");
		}
	}

	private static void mudaDeFila(Processo processo) {
		if(!processo.isFinalizado() &&
				(processosAtivos.contains(processo) || processosBloqueados.contains(processo))) {
			multiplaListaDePrioridade
					.get(PRIORIDADE_MAX - processo.getCreditos()) //quando NA MAXIMA PRIORIDADE RODAR ROUND ROBIN
					.add(processo);
		}
	}

	private static void reduzCreditos(Processo processo) {
		Integer novoCredito = Math.max((processo.getCreditos() - 2), 0);

		CREDITOS_TOTAL = CREDITOS_TOTAL - (processo.getCreditos() - novoCredito);
		processo.setCreditos(novoCredito);
	}

	private static void aumentaQuantum(Processo processo) {
		if(processo.getQuantum() < QUANTUM_MAXIMO){
		if (processo.getQuantum() == 1){
			processo.setQuantum(processo.getQuantum() + 2);
		}else{
			processo.setQuantum(processo.getQuantum() + 1);
		}}
	}

	private static void EscritaLogFile() {
		for (Processo p : processosAtivos) {
			logFile.add("Carregando " + p.getNome());
		}
	}

	public static void executar(int index) {
		totalTroca += 1;
		int numInstrucoes = 0;

		//escrita no logfile
		logFile.add("Executando " + processosAtivos.get(index).getNome());
		processosAtivos.get(index).setCreditos(processosAtivos.get(index).getCreditos() - 1);
		
		//diminui todos da lista de bloq
		descBloq();
		
		//até no maximo tamanho do quantum
		for (int i = 0; i < QUANTUM_MAXIMO; i++) {
			totalTroca += 1;
			numInstrucoes += 1;
			
			//le o comando, deixa pronto para o próximo
			String comando = processosAtivos.get(index).getInstrucoes().get(processosAtivos.get(index).getProgramCounter());
			processosAtivos.get(index).setProgramCounter(processosAtivos.get(index).getProgramCounter() + 1);

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
					logFile.add(processosAtivos.get(index).getNome() + " terminado, " + " X=" + processosAtivos.get(index).getRegistradorX() + " Y=" + processosAtivos.get(index).getRegistradorY());
					processosAtivos.remove(index);
					totalInstrucoes += numInstrucoes;
					return;

				//Atribui os valores das variaveis x e y
				default:
					String result[] = new String[2];
					result = comando.split("=");
					if (comando.contains("X=")) {
						processosAtivos.get(index).setRegistradorX(Integer.parseInt(result[1]));
					} else {
						processosAtivos.get(index).setRegistradorY(Integer.parseInt(result[1]));
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
		QUANTUM_MAXIMO = Integer.parseInt(buff2.readLine());

	}

	public static void descBloq() {
		//metódo para decrementar o bloq
		ArrayList<Processo> novaProcessoBloqueado = new ArrayList<>();

		for (Processo p : processosBloqueados) {
			p.setBloq(p.getBloq() - 1 );

			if (p.getBloq() == 0) {
				processosAtivos.add(p);
			} else {
				novaProcessoBloqueado.add(p);
			}
		}

		processosBloqueados = novaProcessoBloqueado;
	}
		
	public static void redestribuir() {
		// metodo para redistribuir os creditos
		CREDITOS_TOTAL = 0;

		for (Processo p : processosAtivos) {
			CREDITOS_TOTAL += p.getPrioridade();
			p.setCreditos(p.getPrioridade());
		}
		
		for (Processo p : processosBloqueados) {
			CREDITOS_TOTAL += p.getPrioridade();
			p.setCreditos(p.getPrioridade());
		}
	}

	public static void escreverLogFile() throws IOException {
		//método de escrita do log file
		FileWriter arq = new FileWriter(dir + "logfile/log" + QUANTUM_MAXIMO +".txt");
		BufferedWriter gravarArq = new BufferedWriter(arq);
		
		for (String s : logFile) {
			arq.write(s + "\n");
		}

		arq.close();
	}
}
