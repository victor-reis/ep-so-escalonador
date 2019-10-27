import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

//classe que é tabela bcp
public class Processo implements Comparable<Processo> {
    private String nome;
	private ArrayList<String> instrucoes;
	private Integer index = 0;
	private int x = 0;
    private int y = 0;
    private int bloq = 0;
    private Integer creditos = 0;
    private Integer prioridade;
    private boolean finalizado = false;
    
	public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the bloq
     */
    public int getBloq() {
        return bloq;
    }

    /**
     * @param bloq the bloq to set
     */
    public void setBloq(int bloq) {
        this.bloq = bloq;
    }

    /**
     * @return the finalizado
     */
    public boolean isFinalizado() {
        return finalizado;
    }

    /**
     * @param finalizado the finalizado to set
     */
    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(int prioridade) {
		this.prioridade = prioridade;
    }
    
	@Override
	public int compareTo(Processo o) {
		// metodo para comparar creditos e caso de empate comparar nomes
		if (this.creditos.compareTo(o.creditos) == 1)
			return -1;
		else if (this.creditos.compareTo(o.creditos) == 0)
			return this.index.compareTo(o.index);
		else
		    return 1;
    } 
    
	public ArrayList<String> getInstrucoes() {
		return instrucoes;
	}

	public void setInstrucoes(ArrayList<String> instrucoes) {
		this.instrucoes = instrucoes;
    }
    
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
    }
    
	public Integer getCreditos() {
		return creditos;
	}

	public void setCreditos(Integer creditos) {
		this.creditos = creditos;
    }
}
