
import java.util.Random;
import java.util.Scanner;

public class App {

    private Scanner sc;
    private final int ASCII_TO_INT_CONVERTION_OFFSET = 65;
    private final String BOMBA = "X  ";
    private final String PORTA_AVIOES = "P  ";
    private final String CRUZADORES = "C  ";
    private final String DESTROYERS = "D  ";
    private final String SUBMARINOS = "S  ";
    private final int MAXIMO_TENTATIVAS = 30;
    private String reportTurnStatus = " ";
    private int tentativas = 0;
    private int acertos = 0;
    private int[] naviosDestruidos = new int[5];
    private boolean[][] bombardeados = new boolean[8][8];
    private Integer[][] origem = new Integer[8][8]; // Guarda as celulas de origem e o valor que indica se foi gerado pra direita ou pra baixo (Equivalente a z, 0 ou 1)
    private String[][] tabuleiroOculto = new String[8][8]; // contem os navios, inicializado no construtor randomicamente
    private String[][] tabuleiroDescoberto = { // O que sera printado na tela em cada rodada(modificado conforme o jogo avanca)
        {"   ", "1  ", "2  ", "3  ", "4  ", "5  ", "6  ", "7  ", "8  "},
        {"A  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"B  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"C  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"D  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"E  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"F  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"G  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},
        {"H  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  ", "~  "},};

    public App() {
        this.sc = new Scanner(System.in);
        fillBoard();
        System.out.println("======= BATALHA NAVAL =======");
        do {
            printBoard();
            printStatus();

            int[] posicao;
            do { 
                posicao = validarPosicao(sc);
            } while (posicao == null);
            
            executarJogada(posicao[0], posicao[1]);

        } while (!acabou());
        printBoard();
        printStatus();

        finalScore();
        System.out.println("Aperte 's' para revelar os navios escondidos");
        if (sc.next().charAt(0) == 's') {
            revelarPosicoes();
        }
        System.out.println("Obrigado por jogar !!! ");
        sc.close();
    }

    private int[] validarPosicao(Scanner sc) {

        System.out.println("Digite no seguinte formato: Linha(A-H)Coluna(1-8)");
        String coordenada = sc.nextLine();

        if (coordenada.equalsIgnoreCase("PROFESSIONALTOOLS")) { // Cheatcode
            souUmCanalhaTrapaceiro();
            return null;
        }
        if (coordenada.length() != 2) { // Evitar problema de BIOS
            System.out.println("Posicao invalida");
            return null;
        }
        int linhaAux = (int) Character.toUpperCase(coordenada.charAt(0)) - ASCII_TO_INT_CONVERTION_OFFSET; // Converte "A-H" para um inteiro de 0-7
        int colunaAux = Character.getNumericValue(coordenada.charAt(1)) - 1; // Converte "1-8" para um inteiro 0-7

        if (!(linhaAux < 8 && linhaAux >= 0 && colunaAux < 8 && colunaAux >= 0)) {
            System.out.println("Posicao invalida");
            return null;

        } else if (bombardeados[linhaAux][colunaAux]) {
            System.out.println("Ja foi bombardeado, escolha outra posicao");
            return null;
        }

        return new int[]{linhaAux, colunaAux};

    }

    private int returnOffset(String s) { // Retorna o tamanho da "cauda"  do navio, usando em algumas logicas
        switch (s) {
            case PORTA_AVIOES:
                return 3;
            case CRUZADORES:
                return 2;
            case DESTROYERS:
                return 1;
            case SUBMARINOS:
                return 0;

            default:
                System.out.println("Erro!");
                return -1;
        }
    }

    private String converteNome(String s) { // Meramente usado pra printar o nome na tela quando um barco for destruido
        switch (s) {
            case PORTA_AVIOES:
                return "Porta avioes";
            case CRUZADORES:
                return "Cruzador";
            case DESTROYERS:
                return "Destroyer";
            case SUBMARINOS:
                return "Submarino";

            default:
                return "Erro";
        }
    }

    private void verificarNaviosDestruidos() { // Se pudesse usar classe e objeto nao ia precisar dessa naba aqui mas beleza
        for (int i = 0; i < origem.length; i++) {
            for (int j = 0; j < origem.length; j++) {
                if (origem[i][j] != null && origem[i][j].equals(0)) { // Achou um ponto de origem (celula de onde o navio foi "gerado") entra aqui se for ao longo das linhas (vertical)
                    int offset = returnOffset(tabuleiroOculto[i][j]); // Pega o tamanho do navio para ser usado na logica
                    while (offset >= 0) {
                        if (!bombardeados[i + offset][j]) { //Sabe onde comeca, sabe como foi gerado(ou seja, sabe onde termina), verifica se o navio todo foi bombaerdeado
                            break;
                        } else {
                            offset--;
                        }
                    }
                    if (offset < 0) { // ficou ate o final do loop, siginifica que o barco foi destruido
                        naviosDestruidos[4]++; // contagem total de navios destruidos
                        naviosDestruidos[returnOffset(tabuleiroOculto[i][j])]++; // contagem para navios individuais
                        origem[i][j] = null; // Destroi a origem para que o barco nao seja contado novamente
                        reportTurnStatus = reportTurnStatus + "\nAFUNDOU!!!, Você destruiu um " + converteNome(tabuleiroOculto[i][j]) + "!!!";

                    }

                } else if (origem[i][j] != null && origem[i][j].equals(1)) { //Mesma logica de cima para navios gerados horizontalmente
                    int offset = returnOffset(tabuleiroOculto[i][j]);
                    while (offset >= 0) {
                        if (!bombardeados[i][j + offset]) {
                            break;
                        } else {
                            offset--;
                        }
                    }
                    if (offset < 0) {
                        naviosDestruidos[4]++;
                        naviosDestruidos[returnOffset(tabuleiroOculto[i][j])]++;
                        origem[i][j] = null;
                        reportTurnStatus = reportTurnStatus + "\nAFUNDOU!!!, Você destruiu um " + converteNome(tabuleiroOculto[i][j]) + "!!!";

                    }

                }
            }
        }

    }

    private void executarJogada(int linha, int coluna) {
        bombardeados[linha][coluna] = true;
        if (tabuleiroOculto[linha][coluna] != null) {
            reportTurnStatus = "ACERTOU!!!, um navio foi atingido!";
            acertos++;
            tabuleiroDescoberto[linha + 1][coluna + 1] = tabuleiroOculto[linha][coluna];
            verificarNaviosDestruidos();
        } else {
            reportTurnStatus = "ERROU!!!";
            tabuleiroDescoberto[linha + 1][coluna + 1] = BOMBA;

        }
        tentativas++;
    }

    private double taxaDeAcerto() {
        if (tentativas != 0) {
            return (double) acertos / tentativas * 100.0;
        } else {
            return 0.0;
        }
    }

    private void printStatus() {
        System.out.print("Tentativa: " + tentativas + "/" + MAXIMO_TENTATIVAS + "  |  Acertos: " + acertos + "  |  Navios afundados: " + naviosDestruidos[4]);
        System.out.printf("  |  Taxa: %.2f", taxaDeAcerto());
        System.out.println("%");
        System.out.println(reportTurnStatus + "\n-----------------");
        reportTurnStatus = " ";
    }

    private void printBoard() {
        for (int i = 0; i < tabuleiroDescoberto.length; i++) {
            System.out.println();
            for (int j = 0; j < tabuleiroDescoberto.length; j++) {
                System.out.print(tabuleiroDescoberto[i][j]);
            }
        }
        System.out.println("\n");
    }

    private void revelarPosicoes() { // Se quiser printar o tabuleiroOculto antes do final do jogo favor usar a funcao souUmCanalhaTrapaceiro()

        for (int i = 0; i < tabuleiroOculto.length; i++) {
            for (int j = 0; j < tabuleiroOculto.length; j++) {
                if (tabuleiroOculto[i][j] != null) {
                    tabuleiroDescoberto[i + 1][1 + j] = tabuleiroOculto[i][j];
                }
            }
        }
        printBoard();
    }

    private void souUmCanalhaTrapaceiro() {
        System.out.println("====== Bem vindo trapaceiro ==========");
        for (int i = 0; i < tabuleiroOculto.length; i++) {
            System.out.println();
            for (int j = 0; j < tabuleiroOculto.length; j++) {
                if (tabuleiroOculto[i][j] == null) {
                    System.out.print("~  ");
                } else {
                    System.out.print(tabuleiroOculto[i][j]);
                }

            }
        }
        System.out.println("\n===============================");
    }

    private void fillBoard() {
        Random rand = new Random();
        placeP(rand, rand.nextInt(2)); // Coloca o porta avioes, primeiro colocado por tanto tem uma logica mais simples
        int aux = 0;//Usado no loop para colocar N barcos de cada tipo

        // Pra baixo se tem um metodo generico que passa o offset(largura do navio - 1) e o nome, os coloca entao na board oculta 
        // Salvo a chamada da funcao que verifica a disponibilidade antes de inserir no board, tem a mesma logica de placeP()
        while (aux < 2) {
            if (placeGeneric(rand, rand.nextInt(2), returnOffset(CRUZADORES), CRUZADORES)) {
                aux++;
            }
        }
        aux = 0;
        while (aux < 3) {
            if (placeGeneric(rand, rand.nextInt(2), returnOffset(DESTROYERS), DESTROYERS)) {
                aux++;
            }
        }
        aux = 0;
        while (aux < 4) {
            if (placeGeneric(rand, rand.nextInt(2), returnOffset(SUBMARINOS), SUBMARINOS)) {
                aux++;
            }
        }

    }

    private void placeP(Random rand, int z) {

        if (z == 0) { // Caso Z de 0 randomiza um valor de n-3 linhas pra ser a celula geradora, e
            // gera um Porta-avioes verticalmente da celula de origem sempre para baixo ( por isso de 0 a tabuleiroOculto.lenght-offset-1 )
            int offset = 3;
            int randLinha = rand.nextInt(tabuleiroOculto.length - offset);
            int randColuna = rand.nextInt(tabuleiroOculto.length);
            int aux = 0;

            while (aux <= offset) {
                tabuleiroOculto[randLinha + aux][randColuna] = PORTA_AVIOES;
                aux++;
            }
            origem[randLinha][randColuna] = z;

        }
        if (z == 1) { // Mesma logica porem para gerar horizontalmente ( ao longo das colunas, sempre pra direita da celula de origem)
            int offset = 3;
            int randLinha = rand.nextInt(tabuleiroOculto.length);
            int randColuna = rand.nextInt(tabuleiroOculto.length - offset);
            int aux = 0;
            while (aux <= offset) {
                tabuleiroOculto[randLinha][randColuna + aux] = PORTA_AVIOES;
                aux++;

            }
            origem[randLinha][randColuna] = z;
        }

    }

    private boolean verificaPraBaixo(int randLinha, int randColuna, int offset) {
        for (int i = 0; i <= offset; i++) {
            if (tabuleiroOculto[randLinha + i][randColuna] != null) {
                return false;
            }
        }
        return true;
    }

    private boolean verificaPraDireita(int randLinha, int randColuna, int offset) {
        for (int i = 0; i <= offset; i++) {
            if (tabuleiroOculto[randLinha][randColuna + i] != null) {
                return false;
            }
        }
        return true;
    }

    private boolean placeGeneric(Random rand, int z, int offset, String g) {

        if (z == 0) {

            int randLinha = rand.nextInt(tabuleiroOculto.length - offset);
            int randColuna = rand.nextInt(tabuleiroOculto.length);
            int aux = 0;

            if (verificaPraBaixo(randLinha, randColuna, offset)) { // Verifica se as celulas estao livres ao longo do comprimento do navio antes de colocar
                while (aux <= offset) {
                    tabuleiroOculto[randLinha + aux][randColuna] = g;
                    aux++;
                }
                origem[randLinha][randColuna] = z;

                return true;
            }

        }
        if (z == 1) {

            int randLinha = rand.nextInt(tabuleiroOculto.length);
            int randColuna = rand.nextInt(tabuleiroOculto.length - offset);
            int aux = 0;
            if (verificaPraDireita(randLinha, randColuna, offset)) { // Mesma coisa, ao longo das colunas
                while (aux <= offset) {
                    tabuleiroOculto[randLinha][randColuna + aux] = g;
                    aux++;

                }
                origem[randLinha][randColuna] = z;

                return true;
            }

        }

        return false;
    }

    public static void main(String[] args) throws Exception {
        new App();

    }

    private boolean acabou() {
        if (naviosDestruidos[4] == 10 || tentativas == MAXIMO_TENTATIVAS) {
            return true;
        }
        return false;
    }

    private void finalScore() {
        System.out.println("============FINAL SCORE============\n");
        if (tentativas == MAXIMO_TENTATIVAS) {
            System.out.println("Status: Derrota =( ");
        } else {
            System.out.println("Status: VITORIA!!!!");
        }
        System.out.println("Tentativas usadas: " + tentativas + "/30");
        System.out.println("Total de acertos: " + acertos);
        System.out.println("Total de erros: " + (tentativas - acertos));
        System.out.println("Taxa de acerto: " + taxaDeAcerto() + "%");
        System.out.println("Navios afundados: " + naviosDestruidos[4] + "/10");
        System.out.println("  -Porta avioes: " + naviosDestruidos[3] + "/1");
        System.out.println("  -Cruzadores: " + naviosDestruidos[2] + "/2");
        System.out.println("  -Destroyers: " + naviosDestruidos[1] + "/3");
        System.out.println("  -Submarinos: " + naviosDestruidos[0] + "/4");
        int pontuacaoAcertos = acertos * 10;
        int pontuacaoNaviosDestruidos = naviosDestruidos[4] * 50;
        int penalidadeErros = (tentativas - acertos) * -2;
        int bonusVitoriaRapida;
        if (tentativas < 25) {
            bonusVitoriaRapida = 100;
        } else {
            bonusVitoriaRapida = 0;
        }
        int pontuacaoFinal = pontuacaoAcertos + pontuacaoNaviosDestruidos + penalidadeErros + bonusVitoriaRapida;
        System.out.println("\n PONTUACAO FINAL: " + pontuacaoFinal);
        System.out.println("  -Acertos: " + acertos + " x 10 = " + pontuacaoAcertos);
        System.out.println("  -Navios afundados: " + naviosDestruidos[4] + " x 50 = " + pontuacaoNaviosDestruidos);
        System.out.println("  -Penalidade erros: " + (tentativas - acertos) + " x -2 = " + penalidadeErros);
        System.out.println("  -Bonus de vitoria rapida: " + bonusVitoriaRapida);
        System.out.print("\nClassificacao: ");
        if (pontuacaoFinal > 400) {
            System.out.println("EXCELENTE!!!");

        } else if (pontuacaoFinal >= 300) {
            System.out.println("Bom!");
        } else if (pontuacaoFinal >= 200) {
            System.out.println("Paia =| ");

        } else {
            System.out.println("Fezes =/");
        }

    }

}
