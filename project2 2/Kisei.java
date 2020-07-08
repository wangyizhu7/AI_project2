import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Kisei{
    private static int[][] chessBoard;
    private static HashSet<Point> toJudge=new HashSet<>(); // contains all the positions where the algorithm will put the next chess
    private static int[] dr=new int[]{-1,1,-1,1,0,0,-1,1};
    private static int[] dc=new int[]{1,-1,-1,1,-1,1,0,0}; // these two arrays are used to represent 8 direction of a position
    private static final int MAXN=1<<28;
    private static final int MINN=-MAXN;
    private static int size; // size of board
    private static int k; // target
    private static boolean isFinished=false; // use to judge if the program should end the game
    private static String myTeamId = "1124";
    private static String gameId;
    private static int searchDeep = 4;
    public static int waitTime = 6000; // the time for opponent turn, you can change it as you like

    public static void main(String[] args) throws InterruptedException {
        start();
    }

    private static void start() throws InterruptedException {
        int createGameOrAcceptGame = 1; // 1 means create a game and 2 means accept game, you can change it as you like

        String anotherTeamId = "1138"; // this variable represents the id of the opponent team

        //you may use and change the following two variables if you want to change the board size and game target
        int boardSize = 12;
        int target = 6;

        //you may use and change the following variable if you want to accept a game
        // id of accepted game
        String acceptedGameId = "1964";

        if (createGameOrAcceptGame == 1){
            createGame(anotherTeamId,boardSize,target);
            while(true){
                Thread.sleep(waitTime);
                while (!getMove(gameId)){
                    System.out.println("You can finish the game or wait another a wait time!");
                    Thread.sleep(waitTime);
                }
                // judge if the newest move ends the game
                if(isFinished){
                    System.out.println("The opponent wins!");
                    return;
                }
                // judge if the board is full
                if(toJudge.size() == 0){
                    System.out.println("Draw! Game Over!");
                    return;
                }
                System.out.println("Team 1124 turn");
                Node node = new Node();
                dfs(0,node,MINN,MAXN);
                int x = node.bestChild.p.x;
                int y = node.bestChild.p.y;
                makeMove(y,x);
                chessBoard[y][x] = 1;
                if(isEnd(x,y)){
                    System.out.println("I WIN!!!");
                    return;
                }
                Point temp = new Point(x,y);
                toJudge.remove(temp);
                addAdjacentPositionToJudgeSet(x,y);
                // judge if the board is full
                if(toJudge.size() == 0){
                    System.out.println("Draw! Game Over!");
                    return;
                }
            }
        }else if(createGameOrAcceptGame == 2){
            acceptGame(acceptedGameId);
            if(isFinished) return;
            while(true){
                System.out.println("Team 1131 turn");
                Node node = new Node();
                dfs(0,node,MINN,MAXN);
                int x = node.bestChild.p.x;
                int y = node.bestChild.p.y;
                makeMove(y,x);
                chessBoard[y][x] = 1;
                if(isEnd(x,y)){
                    System.out.println("I WIN!!!");
                    return;
                }
                Point temp = new Point(x,y);
                toJudge.remove(temp);
                addAdjacentPositionToJudgeSet(x,y);
                if(toJudge.size() == 0){
                    System.out.println("Draw! Game Over!");
                    return;
                }
                Thread.sleep(waitTime);
                while (!getMove(gameId)){
                    System.out.println("You can finish the game or wait another wait time!");
                    Thread.sleep(waitTime);
                }
                if(isFinished){
                    System.out.println("The opponent wins!");
                    return;
                }
                if(toJudge.size() == 0){
                    System.out.println("Draw! Game Over!");
                    return;
                }
            }
        }

    }

    /**
     * create a game and make first move
     * @param anotherTeamId the id of the team of the opponent
     * @param boardSize the size of the chess board, default 12
     * @param target the number of chess pieces in a line to win
     */
    private static void createGame(String anotherTeamId, int boardSize, int target){
        String param = "teamId1=" + myTeamId + "&teamId2=" + anotherTeamId + "&type=game&gameType=TTT&boardSize=" + boardSize + "&target=" + target;
        String res = HttpClient.sendPost(param); // send POST request to create the game
        String regex = ".*Id\":(.*)}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(res);
        if(!m.find()){
            System.out.println("Wrong JASON format, cannot extract game id");
            isFinished = true;
            return;
        }
        gameId = m.group(1);
        size = boardSize;
        k = target;
        chessBoard = new int[size][size];
        System.out.println("Create a game successfully! The gameId is " + gameId);
        int a = size/2;
        makeMove(a,a);
        chessBoard[a][a] = 1;
        addAdjacentPositionToJudgeSet(a,a);
    }

    /**
     * Accept a game, the game must be created already
     * @param gameId1 id of the game
     * @throws InterruptedException
     */
    private static void acceptGame(String gameId1) throws InterruptedException {
        gameId = gameId1;
        String[] eachRow = getBoardString(gameId);
        size = eachRow[0].length();
        // judge if got the true string of board
        if(size != eachRow.length){
            System.out.println("The number of rows of the board is not equal to the number of columns!");
            isFinished = true;
            return;
        }
        chessBoard = new int[size][size];
        int count = 0; // record the number of chess pieces
        // initial the chess board
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(eachRow[i].charAt(j) != '-'){
                    count++;
                    if(count > 1){
                        isFinished = true;
                        System.out.println("The initial chess pieces are more than 1, I refuse to continue the game!");
                    }
                    chessBoard[i][j] = -1;
                    addAdjacentPositionToJudgeSet(i,j);
                }
            }
        }
        // if there is not piece in the board, the algorithm will wait util the opponent put the first chess
        while(count == 0){
            System.out.println("The opponent hasn't put any pieces! You can end the program or you can wait another wait time!");
            Thread.sleep(waitTime);
            eachRow = getBoardString(gameId);
            if(size != eachRow.length){
                System.out.println("The number of rows of the board is not equal to the number of columns! Game finished!");
                isFinished = true;
                return;
            }
            for(int i = 0; i < size; i++){
                for(int j = 0; j < size; j++){
                    if(eachRow[i].charAt(j) != '-'){
                        count++;
                        if(count > 1){
                            isFinished = true;
                            System.out.println("The initial chess pieces are more than 1, I refuse to continue the game!");
                        }
                        chessBoard[i][j] = -1;
                        addAdjacentPositionToJudgeSet(i,j);

                    }
                }
            }
        }
    }

    /**
     * sent the move the algorithm made to the server
     * @param y the position of row
     * @param x the position of column
     */
    private static void makeMove(int y, int x){
        String move = y + "," + x;
        String param = "teamId=" + myTeamId + "&move=" + move + "&type=move&gameId=" + gameId;
        HttpClient.sendPost(param);
        System.out.println("we make move" + y + "," + x);
    }

    /**
     * Get the recent one move, if the move is belong to opponent, make changes, otherwise, sleep extra wait time to wait
     * the opponent
     * @param gameId id of the game
     * @return true or false
     */
    private static boolean getMove(String gameId){
        String param = "type=moves&gameId=" + gameId + "&count=1";
        String res = HttpClient.sendGet(param);
        String regex = ".*ve\":\"(.*),(.*)\",\".*l\":\"(\\S)\".*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(res);
        if(!m.find()){
            System.out.println("Wrong JASON format, cannot extract the information of moves");
        }
        String y = m.group(1);
        String x = m.group(2);
        int xPos = Integer.valueOf(x);
        int yPos = Integer.valueOf(y);
        System.out.println("The opponent move " + y + "," + x);
        if(chessBoard[yPos][xPos] != 0){
            System.out.println("The opponent hasn't ended his turn!!!");
            return false;
        }else{
            chessBoard[yPos][xPos] = -1;
            if(isEnd(xPos,yPos)){
                isFinished = true;
            }else{
                Point temp = new Point(xPos,yPos);
                toJudge.remove(temp);
                addAdjacentPositionToJudgeSet(xPos,yPos);
            }
            return true;
        }
    }

    // alpha beta dfs
    private static void dfs(int depth,Node root,int alpha,int beta){
        if(depth== searchDeep){
            root.mark=h();
            return;
        }
        ArrayList<Point> judgeSet=new ArrayList<>();
        Iterator it=toJudge.iterator();
        while(it.hasNext()){
            Point now=new Point((Point)it.next());
            judgeSet.add(now);
        }
        it=judgeSet.iterator();
        while(it.hasNext()){
            Point now=new Point((Point)it.next());
            Node node=new Node();
            node.setPoint(now);
            root.addChild(node);
            boolean flag=toJudge.contains(now);
            chessBoard[now.y][now.x]=((depth&1)==1)?-1:1;
            if(isEnd(now.x,now.y)){
                root.bestChild=node;
                root.mark=MAXN*chessBoard[now.y][now.x];
                chessBoard[now.y][now.x]=0;
                return;
            }

            boolean[] flags=new boolean[8];
            Arrays.fill(flags,true);
            for(int i=0;i<8;i++){
                Point next=new Point(now.x+dc[i],now.y+dr[i]);
                if(0<=now.x+dc[i] && now.x+dc[i]<size && 0<=now.y+dr[i] && now.y+dr[i]<size && chessBoard[next.y][next.x]==0){
                    if(!toJudge.contains(next)){
                        toJudge.add(next);
                    }
                    else flags[i]=false;
                }
            }
            if(flag)
                toJudge.remove(now);
            dfs(depth+1,root.getLastChild(),alpha,beta);
            chessBoard[now.y][now.x]=0;
            if(flag)
                toJudge.add(now);
            for(int i=0;i<8;++i)
                if(flags[i])
                    toJudge.remove(new Point(now.x+dc[i],now.y+dr[i]));
            //min
            if((depth&1)==1){
                if(root.bestChild==null || root.getLastChild().mark<root.bestChild.mark){
                    root.bestChild=root.getLastChild();
                    root.mark=root.bestChild.mark;
                    if(root.mark<=MINN)
                        root.mark+=depth;
                    beta=Math.min(root.mark,beta);
                }
                if(root.mark<=alpha)
                    return;
            }
            //max
            else{
                if(root.bestChild==null || root.getLastChild().mark>root.bestChild.mark){
                    root.bestChild=root.getLastChild();
                    root.mark=root.bestChild.mark;
                    if(root.mark==MAXN)
                        root.mark-=depth;
                    alpha=Math.max(root.mark,alpha);
                }
                if(root.mark>=beta)
                    return;
            }
        }
    }

    // evaluate method
    private static int h(){
        int res=0;
        for(int i=0;i<size;++i){
            for(int j=0;j<size;++j){
                if(chessBoard[i][j]!=0){
                    boolean flag1=false,flag2=false;
                    int x=j,y=i;
                    int cnt=1;
                    int col=x,row=y;
                    while(--col>=0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(col>=0 && chessBoard[row][col]==0) flag1=true;
                    col=x;row=y;
                    while(++col<size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(col<size && chessBoard[row][col]==0) flag2=true;
                    if(flag1 && flag2)
                        res+=chessBoard[i][j]*cnt*cnt;
                    else if(flag1 || flag2) res+=chessBoard[i][j]*cnt*cnt/4;
                    if(cnt>=k) res=MAXN*chessBoard[i][j];
                    col=x;row=y;
                    cnt=1;flag1=false;flag2=false;
                    while(--row>=0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(row>=0 && chessBoard[row][col]==0) flag1=true;
                    col=x;row=y;
                    while(++row<size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(row<size && chessBoard[row][col]==0) flag2=true;
                    if(flag1 && flag2)
                        res+=chessBoard[i][j]*cnt*cnt;
                    else if(flag1 || flag2)
                        res+=chessBoard[i][j]*cnt*cnt/4;
                    if(cnt>=k) res=MAXN*chessBoard[i][j];
                    col=x;row=y;
                    cnt=1;flag1=false;flag2=false;
                    while(--col>=0 && --row>=0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(col>=0 && row>=0 && chessBoard[row][col]==0) flag1=true;
                    col=x;row=y;
                    while(++col<size && ++row<size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(col<size && row<size && chessBoard[row][col]==0) flag2=true;
                    if(flag1 && flag2)
                        res+=chessBoard[i][j]*cnt*cnt;
                    else if(flag1 || flag2) res+=chessBoard[i][j]*cnt*cnt/4;
                    if(cnt>=k) res=MAXN*chessBoard[i][j];
                    col=x;row=y;
                    cnt=1;flag1=false;flag2=false;
                    while(++row<size && --col>=0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(row<size && col>=0 && chessBoard[row][col]==0) flag1=true;
                    col=x;row=y;
                    while(--row>=0 && ++col<size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
                    if(row>=0 && col<size && chessBoard[i][j]==0) flag2=true;
                    if(flag1 && flag2)
                        res+=chessBoard[i][j]*cnt*cnt;
                    else if(flag1 || flag2) res+=chessBoard[i][j]*cnt*cnt/4;
                    if(cnt>=k) res=MAXN*chessBoard[i][j];
                }
            }
        }
        return res;
    }

    // to judge if anyone win the game
    private static boolean isEnd(int x, int y){
        int cnt=1;
        int col=x,row=y;
        // left
        while(--col >= 0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        col=x;row=y;
        // right
        while(++col < size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        if(cnt>=k){
            return true;
        }
        col=x;row=y;
        cnt=1;
        // up
        while(--row >= 0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        col=x;row=y;
        // down
        while(++row < size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        if(cnt>=k){
            return true;
        }
        col=x;row=y;
        cnt=1;
        // upleft
        while(--col >= 0 && --row >= 0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        col=x;row=y;
        // downright
        while(++col < size && ++row < size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        if(cnt>=k){
            return true;
        }
        col=x;row=y;
        cnt=1;
        // downleft
        while(++row < size && --col >= 0 && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        col=x;row=y;
        // upright
        while(--row >= 0 && ++col < size && chessBoard[row][col]==chessBoard[y][x]) ++cnt;
        if(cnt>=k){
            return true;
        }
        return false;
    }

    /**
     * use to add the adjacent positions of (x,y) into tuJudge set
     * @param x x position
     * @param y y position
     */
    private static void addAdjacentPositionToJudgeSet(int x, int y){
        for(int i = 0;i < 8; i++){
            Point now=new Point(x+dc[i],y+dr[i]);
            if(0 <= now.x && now.x < size && 0 <= now.y && now.y < size && chessBoard[now.y][now.x]==0)
                toJudge.add(now);
        }
    }
    /**
     * Get the string of the board, and change it to a string array in which each element represents a row in order
     * @param gameId id of game
     * @return return the string array of the board
     */
    private static String[] getBoardString(String gameId){
        String param = "type=boardString&gameId=" + gameId;
        String res = HttpClient.sendGet(param);
        String regex = ".*ut\":\"(.*)\",\"target\":(\\d).*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(res);
        String chess = "";
        if(m.find()){
            chess = m.group(1);
            String target = m.group(2);
            k = Integer.valueOf(target);
        }else {
            System.out.println("The format of the JASON is wrong!");
            isFinished = true;
        }
        return chess.split("\\\\n");
    }
}
class Node{
    Node bestChild;
    private ArrayList<Node> child=new ArrayList<>();
    Point p=new Point();
    int mark;
    Node(){
        this.child.clear();
        bestChild=null;
        mark=0;
    }
    void setPoint(Point r){
        p.x=r.x;
        p.y=r.y;
    }
    void addChild(Node r){
        this.child.add(r);
    }
    Node getLastChild(){
        return child.get(child.size()-1);
    }
}
