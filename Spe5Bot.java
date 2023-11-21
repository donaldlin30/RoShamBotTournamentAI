
import java.util.Random;
import java.util.Arrays;

public class Spe5Bot implements RoShamBot {
	public int rnd=0;
	public int op_den=0;
	public int netdiff=0;

	public int[][] diffs=new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}}; //oppo4s,den,oppo31
	public int[][] gests=new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}}; //oppo4s,den,oppo31
	public Action actions[]=new Action[]{Action.ROCK,Action.PAPER,Action.SCISSORS,Action.LIZARD,Action.SPOCK};
	public Action record1[]=new Action[2000000]; //array of opponent's previous moves
	public Action record2[]=new Action[2000000]; //array of my previous moves
	
	public int record_oppo4[][][][]=new int[5][5][5][5];  //the number of 4 previous opponent moves of a specifit range of previous moves
	public int record_oppo31[][][][]=new int[5][5][5][5]; //the number of (3 previous opponent move, my second previous move) of a specifit range of previous moves
	
	public int precord_oppo4[][][][]=new int[5][5][5][5];  //(p) the number of 4 previous opponent moves of a specifit range of previous moves
	public int precord_oppo31[][][][]=new int[5][5][5][5]; //(p) the number of (3 previous opponent move, my second previous move) of a specifit range of previous moves

	
	public int my_record[]=new int[]{0,0,0,0,0}; //the number of my previous moves of each type
	public int oppo_record[]=new int[]{0,0,0,0,0}; //the number of opponent's previous moves of each type
	public int battle[][]=new int[][]{{0,-1,1,1,-1},{1,0,-1,-1,1},{-1,1,0,1,-1},{-1,1,-1,0,1},{1,-1,1,-1,0}};
	public int re_den[]=new int[]{3,0,1,4,2}; //what I should do, if the opponents do the density strategy (Cole's strategy); 
	//re_den is not used because it will lead to nash equilibriam in practice
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
	public Action t_index(int a){
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
	public void calc_4s(int ind){ //using last rnd
		if(ind>3)record_oppo4[index(record1[ind-3])][index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[0][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>2000)record_oppo4[index(record1[ind-2000])][index(record1[ind-1999])][index(record1[ind-1998])][index(record1[ind-1997])]--;
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][2]+record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][3];
		scount[1]=record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][0]+record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][4];
		scount[2]=record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][1]+record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][3];
		scount[3]=record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][1]+record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][4];
		scount[4]=record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][0]+record_oppo4[index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])][2];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[0][i]=to_return;to_return=re_den[to_return];}
	}
	public void calc_31(int ind){ //using last rnd
		if(ind>3)record_oppo31[index(record2[ind-1])][index(record1[ind-2])][index(record1[ind-1])][index(record1[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[1][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>2000)record_oppo31[index(record2[ind-1998])][index(record1[ind-1999])][index(record1[ind-1998])][index(record1[ind-1997])]--;
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][2]+record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][3];
		scount[1]=record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][0]+record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][4];
		scount[2]=record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][1]+record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][3];
		scount[3]=record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][1]+record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][4];
		scount[4]=record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][0]+record_oppo31[index(record2[ind])][index(record1[ind-1])][index(record1[ind])][2];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[1][i]=to_return;to_return=re_den[to_return];}
	}
	public void calc_den(){
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=oppo_record[2]+oppo_record[3];
		scount[1]=oppo_record[0]+oppo_record[4];
		scount[2]=oppo_record[1]+oppo_record[3];
		scount[3]=oppo_record[1]+oppo_record[4];
		scount[4]=oppo_record[0]+oppo_record[2];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[2][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_4s(int ind){ //using last rnd
		if(ind>3)precord_oppo4[index(record2[ind-3])][index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[3][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>2000)precord_oppo4[index(record2[ind-2000])][index(record2[ind-1999])][index(record2[ind-1998])][index(record2[ind-1997])]--;
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][2]+precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][3];
		scount[1]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][0]+precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][4];
		scount[2]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][1]+precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][3];
		scount[3]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][1]+precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][4];
		scount[4]=precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][0]+precord_oppo4[index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])][2];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[3][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_31(int ind){ //using last rnd
		if(ind>3)precord_oppo31[index(record1[ind-1])][index(record2[ind-2])][index(record2[ind-1])][index(record2[ind])]++;
		else{
			int to_return=index(nashMove());
			for(int i=0;i<5;i++){gests[4][i]=to_return;to_return=re_den[to_return];}
			return;
		}
		if(ind>2000)precord_oppo31[index(record1[ind-1998])][index(record2[ind-1999])][index(record2[ind-1998])][index(record2[ind-1997])]--;
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][2]+precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][3];
		scount[1]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][0]+precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][4];
		scount[2]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][1]+precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][3];
		scount[3]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][1]+precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][4];
		scount[4]=precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][0]+precord_oppo31[index(record1[ind])][index(record2[ind-1])][index(record2[ind])][2];
		int to_return=0;
		for(int i=1;i<5;i++)if(scount[i]>scount[to_return])to_return=i;
		for(int i=0;i<5;i++){gests[4][i]=to_return;to_return=re_den[to_return];}
	}
	public void pcalc_den(){
		int scount[]=new int[]{0,0,0,0,0};
		scount[0]=my_record[2]+my_record[3];
		scount[1]=my_record[0]+my_record[4];
		scount[2]=my_record[1]+my_record[3];
		scount[3]=my_record[1]+my_record[4];
		scount[4]=my_record[0]+my_record[2];
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
		
		
		to_return=t_index(gests[x][y]); //keep track of my move
		//if(netdiff<-160)to_return=nashMove();
		my_record[index(to_return)]++;
		record2[rnd]=to_return;
		return to_return;
    }
    
}