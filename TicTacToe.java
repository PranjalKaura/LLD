import java.util.*;

public class TicTacToe
{
    public static void main(String[] args) {
        GameController gameController = GameController.getInstance();
        gameController.startGame();
    }
}

class GameController
{
    private static GameController controllerInstance;
    Game game;
    Scanner scan;
    private GameController()
    {
        scan = new Scanner(System.in);
    }

    public static GameController getInstance()
    {
        if(GameController.controllerInstance==null) controllerInstance = new GameController();
        return GameController.controllerInstance;
    }

    public void startGame()
    {
        System.out.print("Please specify the board Size: ");
        int boardSize = scan.nextInt();
        System.out.println();
        System.out.print("Please specify the number of players: ");
        int numPlayers = scan.nextInt();
        System.out.println();
        game = new Game(boardSize, numPlayers, new DefaultWinStrategy());
        game.printGameState();
        while(game.isActive())
        {
            for(int i = 0;i<numPlayers;i++)
            {
                System.out.println("Please specify player " + i + " move.");
                int x = scan.nextInt(); int y = scan.nextInt();
                Move move = new Move(x, y);
                if(!game.checkValidMove(move))
                {
                    System.out.println("Move invalid. Please retry");
                    i--;
                    continue;
                }
                if(game.makeMove(move))
                {
                    System.out.println("PLayer has won the game");
                    break;
                }
            }
            game.printGameState();
        }
    }
}

class Game
{
    private Board board;
    private List<Player> players;
    private boolean active;
    IWinStrategy winStrategy;

    private Queue<Player> playerQ;

    public Game(int boardSize, int numPlayers, IWinStrategy winStrategy)
    {
        board = new Board(boardSize);
        players = new ArrayList<>();
        playerQ = new LinkedList<>();
        for(int i = 0;i<numPlayers;i++)
        {
            String playerName = "Player" + Integer.toString(i);
            Player player = new HumanPlayer(playerName, (char)(i + '0'));
            players.add(player);
            playerQ.add(player);
        }

        this.winStrategy = winStrategy;

        active = true;
    }

    public boolean checkValidMove(Move move)
    {
        return board.checkValidMove(move);
    }

    public boolean makeMove(Move move)
    {
        Player curPlayer = playerQ.poll(); // poll player from start of queue
        curPlayer.makeMove(board, move);
        playerQ.add(curPlayer); // add player back to end of queue
        if(checkPlayerWin(curPlayer))
        {
            active = false;
            return true;
        }
        return false;
    }

    public boolean checkPlayerWin(Player player)
    {
        return winStrategy.checkPlayerWin(board, player);
    }

    public boolean isActive()
    {
        return active;
    }

    public void printGameState()
    {
        System.out.println(board);
    }

}

class Board
{
    public List<List<Character>> board;
    int boardSize;
    public Board(int boardSize)
    {
        this.boardSize = boardSize;
        board = new ArrayList<>();
        for(int i = 0;i<boardSize;i++)
        {
            board.add(new ArrayList<Character>());
            for(int j = 0;j<boardSize;j++)
            {
                board.get(i).add('.');
            }
        }
    }

    public boolean checkValidMove(Move move)
    {
        if(move.x>=boardSize || move.x<0 || move.y>=boardSize || move.y<0) return false;
        if(board.get(move.x).get(move.y)!='.' ) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder('\n');
        for(int i = 0;i<boardSize;i++)
        {
            for(int j = 0;j<boardSize;j++)
            {
                sb.append(board.get(i).get(j));
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

abstract class Player
{
    String name;
    char symbol;

    public Player(String name, char symbol)
    {
        this.name = name;
        this.symbol = symbol;
    }

    public abstract void makeMove(Board board, Move move);
}

class HumanPlayer extends Player
{
    public HumanPlayer(String name, char symbol)
    {
        super(name, symbol);
    }

    public void makeMove(Board board, Move move)
    {
        board.board.get(move.x).set(move.y, symbol);
    }
}

class Move
{
    int x;
    int y;
    public Move(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
}

interface IWinStrategy
{
    public boolean checkPlayerWin(Board board, Player player);
}

class DefaultWinStrategy implements IWinStrategy
{
    public DefaultWinStrategy()
    {

    }

    @Override
    public boolean checkPlayerWin(Board board, Player player) {
        if(checkRowMatch(board, player.symbol)) return true;
        if(checkColMatch(board, player.symbol)) return true;
        if(checkDiagMatch(board, player.symbol)) return true;
        if(checkAntiDiagMatch(board, player.symbol)) return true;
        return false;
    }

    private boolean checkRowMatch(Board board, char symbol)
    {
        for(int i = 0;i<board.boardSize;i++)
        {
            boolean rowMatch = true;
            for(int j = 0;j<board.boardSize;j++)
            {
                if(board.board.get(i).get(j)!=symbol) 
                {
                    rowMatch = false;
                    break;
                }
            }
            if(rowMatch) return true;
        }
        return false;
    }

    private boolean checkColMatch(Board board, char symbol)
    {
        for(int i = 0;i<board.boardSize;i++)
        {
            boolean colMatch = true;
            for(int j = 0;j<board.boardSize;j++)
            {
                if(board.board.get(j).get(i)!=symbol) 
                {
                    colMatch = false;
                    break;
                }
            }
            if(colMatch) return true;
        }
        return false;
    }

    private boolean checkDiagMatch(Board board, char symbol)
    {
        boolean diagMatch = true;
        for(int i = 0;i<board.boardSize;i++)
        {
            if(board.board.get(i).get(i)!=symbol) 
            {
                diagMatch = false;
                break;
            }
        }
        return diagMatch;
    }

    private boolean checkAntiDiagMatch(Board board, char symbol)
    {
        boolean diagMatch = true;
        for(int i = 0;i<board.boardSize;i++)
        {
            if(board.board.get(i).get(board.boardSize-1-i)!=symbol) 
            {
                diagMatch = false;
                break;
            }
        }
        return diagMatch;
    }

    
}