
import java.util.Random;
import java.util.Arrays;

public class Spe6Bot implements RoShamBot { 
	public int rnd=0;
	public int op_den=0;
	public int netdiff=0;
	public int trainsize=2000;
	boolean alwaysnash=false;
	public int[][] diffs=new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}}; 
	//each strategy has five rounds of guessing to form a fully expressive cycle; 
	//we implements 3 basic strategies and allow opponents to use them, so together (3+3)*5 ways to decide next step
	public int[][] gests=new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}}; 
	//what I would do last time if I use each strategy
	public Action record1[]=new Action[2000000]; //array of opponent's previous moves
	public Action record2[]=new Action[2000000]; //array of my previous moves
	
	public int record_oppo4[][][][]=new int[5][5][5][5];  //the number of 4 previous opponent moves of a specifit range of previous moves
	public int record_oppo31[][][][]=new int[5][5][5][5]; //the number of (3 previous opponent move, my second previous move) of a specifit range of previous moves
	
	public int precord_oppo4[][][][]=new int[5][5][5][5];  //(p) the number of 4 previous opponent moves of a specifit range of previous moves
	public int precord_oppo31[][][][]=new int[5][5][5][5]; //(p) the number of (3 previous opponent move, my second previous move) of a specifit range of previous moves
	// the "p" means what the opponent thinks, so "opponent" in the comment is now "me"
	
	public int my_record[]=new int[]{0,0,0,0,0}; //the number of my previous moves of each type
	public int oppo_record[]=new int[]{0,0,0,0,0}; //the number of opponent's previous moves of each type
	public int battle[][]=new int[][]{{0,-1,1,1,-1},{1,0,-1,-1,1},{-1,1,0,1,-1},{-1,1,-1,0,1},{1,-1,1,-1,0}};
	//if I play first index, opponent play next index, will I win or not.
	public int re_den[]=new int[]{3,0,1,4,2}; 
	//If opponent guess I will do this (index), what I should do instead to beat his guess
	
	public int index(Action a){ //given index to actions
		if(a==Action.ROCK)
			return 0;
		if(a==Action.PAPER)
			return 1;
		if(a==Action.SCISSORS)
			return 2;
		if(a==Action.LIZARD)
			return 3;
		if(a==Action.SPOCK)
			return 4;
		return 0;
	}
	public Action t_index(int a){ //retrieve actions from index
		if(a==0)
			return Action.ROCK;
		if(a==1)
			return Action.PAPER;
		if(a==2)
			return Action.SCISSORS;
		if(a==3)
			return Action.LIZARD;
		if(a==4)
			return Action.SPOCK;
		return Action.ROCK;
	}

	public Action nashMove(){ //random strategy
		double coinFlip = Math.random();
        
        if (coinFlip <= 1.0/5.0)
            return Action.ROCK;
        else if (coinFlip <= 2.0/5.0)
            return Action.PAPER;
        else if (coinFlip <= 3.0/5.0)
            return Action.SCISSORS;
        else if (coinFlip <= 4.0/5.0)
            return Action.LIZARD;
        else 
            return Action.SPOCK;
	}
	public int[] calc(int arr[]){ 
	//arr is the array of opponent's "probability" to do each guestion. scount is expected probability win minus loss if I do something
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=arr[2]+arr[3]-arr[1]-arr[4];
		scount[1]=arr[0]+arr[4]-arr[2]-arr[3];
		scount[2]=arr[1]+arr[3]-arr[0]-arr[4];
		scount[3]=arr[1]+arr[4]-arr[0]-arr[2];
		scount[4]=arr[0]+arr[2]-arr[1]-arr[3];
		return scount;
	}
	public void calc_4s(int ind){ //I use opponents' previous 4 consecutive to guess what he do
		if(ind>3)record_oppo4[index(record1[ind-3])][index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[0][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>trainsize)record_oppo4[index(record1[ind-trainsize])][index(record1[ind-(trainsize-1)])][index(record1[ind-(trainsize-2)])][index(record1[ind-(trainsize-3)])]--;
		int scount[]=calc(record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])]);
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[0][i]=to_return;to_return=re_den[to_return];}//update the full cycle
	}
	public void calc_31(int ind){ //I use opponents' previous 3 consecutive moves together with 1 my previous move to guess what he do
		if(ind>3)record_oppo31[index(record2[ind-1])][index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[1][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>trainsize)record_oppo31[index(record2[ind-(trainsize-2)])][index(record1[ind-(trainsize-1)])][index(record1[ind-(trainsize-2)])][index(record1[ind-(trainsize-3)])]--;
		int scount[]=calc(record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])]);
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[1][i]=to_return;to_return=re_den[to_return];}
	}
	public void calc_den(){//I use the number of each type of guestures played by the opponent to guess what he will do
		int scount[]=calc(oppo_record);
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[2][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_4s(int ind){ //opponent use my previous 4 consecutive to guess what I do
		if(ind>3)precord_oppo4[index(record2[ind-3])][index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[3][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>trainsize)precord_oppo4[index(record2[ind-trainsize])][index(record2[ind-(trainsize-1)])][index(record2[ind-(trainsize-2)])][index(record2[ind-(trainsize-3)])]--;
		int scount[]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[3][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_31(int ind){ //opponent use my previous 3 consecutive moves together with 1 his previous move to guess what I do
		if(ind>3)precord_oppo31[index(record1[ind-1])][index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[4][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>trainsize)precord_oppo31[index(record1[ind-(trainsize-2)])][index(record2[ind-(trainsize-1)])][index(record2[ind-(trainsize-2)])][index(record2[ind-(trainsize-3)])]--;
		int scount[]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[4][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_den(){//opponent use the number of each type of guestures played by me to guess what I will do
		int scount[]=my_record;
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[5][i]=to_return;to_return=re_den[to_return];}
	}

    public Action getNextMove(Action lastOpponentMove) {
		record2[0]=Action.ROCK;
		Action to_return; //what I will do this move
        record1[rnd]=lastOpponentMove;
		oppo_record[index(lastOpponentMove)]++;
		netdiff+=battle[index(record2[rnd])][index(lastOpponentMove)];
			
		for(int i=0;i<6;i++)for(int j=0;j<5;j++)diffs[i][j]+=battle[gests[i][j]][index(lastOpponentMove)];
		// expected net win if I have used each strategy consistently
		calc_4s(rnd);
		calc_31(rnd);
		calc_den();		
		pcalc_4s(rnd);
		pcalc_31(rnd);
		pcalc_den();

		rnd++;
		if(rnd<500){ //behave randomly in first 500 tests to see what opponent will do
			to_return=nashMove();
			my_record[index(to_return)]++;
			record2[rnd]=to_return;
			return to_return;
		}
		int x=0;
		int y=0;
		for(int i=0;i<6;i++)for(int j=0;j<5;j++)if(diffs[i][j]>diffs[x][y]){x=i;y=j;}
		//use the strategy that has the largest expected net win so far
				
		to_return=t_index(gests[x][y]); //keep track of my move
		if(netdiff<Math.min(-160,rnd*3/200*-1))alwaysnash=true;
		//System.out.println(Math.min(-160,rnd*3/200*-1));
		if(alwaysnash)to_return=nashMove();

		//stop loss line
		my_record[index(to_return)]++;
		record2[rnd]=to_return;
		return to_return;
    }    
}