#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

struct process{
	int pn; //process number
	int cput; //cpu time that has already run
	int ft; //finished time
	int io; //I/O time
	int wt; //waiting time
	int tl; //time left, associate with status
	int rt; //running time
	int prevcpu;
	int * details; //(A,B,C,M)
	char *status; //unstarted, ready, running, blocked, or terminated
	struct process * next; //next process
};

struct ready{
	struct process * pro;
	struct ready * next;
};

FILE * randnum;
int nrand = 0;

int randomOS(int u){
	if (!randnum){
		printf("An error occurs while open the file.");
		exit(1);
	}
	long cr = 0; //current random
	if (fscanf(randnum, "%ld", &cr) != 1){
		printf("Error reading the %dth integer", nrand);
		exit(1);
	}
	nrand += 1;
	//printf("%ld\n", cr);
	int r = 1 + (cr % u);
	return r;
}

void add_to_queue(struct ready * rqh, struct process * cp){
	struct ready * rqt = malloc(sizeof(struct ready *)); //ready queue tail
	struct ready * node = malloc(sizeof(struct ready)); //new node for process;

	rqt = rqh;
	bool exist = false;
	while (rqt -> next != NULL){
		rqt = rqt -> next;
		if (rqt -> pro == cp){
			exist = true;
		}
	}
	if (exist == false){
		node -> pro = cp;
		node -> next = NULL;
		rqt -> next = node;
		rqt = rqt -> next; 							
	}
}

void print_queue(struct ready * rqh){
	struct ready * rqt = malloc(sizeof(struct ready));
	rqt = rqh;
	printf(" [");
	while (rqt != NULL){
		if (rqt->pro){
			printf(" '%d' ", rqt -> pro -> pn);
		}		
		rqt = rqt -> next;
	}
	printf("] ");

}

void update_time_left(struct process * cp){
	cp -> tl = randomOS((cp -> details)[1]);
	if (cp -> cput + cp -> tl > (cp -> details)[2]){
		cp -> tl = (cp -> details)[2] - (cp -> cput);
	}
	cp -> prevcpu = cp -> tl;

	//printf("update time for %d here!", cp ->pn);
}

#include "FCFS.h"

#include "RR.h"

#include "UNI.h"

#include "SJF.h"

int main(int argc, char * argv[]){
	FILE * fp;
	bool v = false;
	int algorithm = 0;
	if (argc == 2){
		fp = fopen(argv[1],"r");
	}
	else{
		if (argc == 3){
			fp = fopen(argv[2],"r");
			v = true;
		}
		else{
			printf("Wrong numbers of input. Please try again.");
			exit(1);
		}
	}

	// printf("Please specify the algorithms that you want to use\n\n");
	// printf("Choices are:\n");
	// printf("1. First come first serve\n");
	// printf("2. Round Robbin\n");
	// printf("3. Uniprocessing\n");
	// printf("4. Shortest Job First\n\n");
	// printf("Please choose algorithms by input the number 1, 2, 3, or 4:\n");
	// scanf("%d", &algorithm);

	int num = 0; //4 num per process
	int count = 0; //the #{count}'s process
	int tp; //total process
	char attr[5]; // the content of four attributes
	
	fscanf(fp, "%d", &tp);
	struct process * queue[tp];
	struct process * cur_pro = malloc(sizeof(struct process));
	cur_pro -> details = malloc(4 * sizeof(int));	

	printf("The original input was: %d ", tp);
	while (fp){
		fscanf(fp, "%s", attr);
		printf("%s ", attr);

		if (!cur_pro){
			cur_pro = malloc(sizeof(struct process));
			cur_pro -> details = malloc(4 * sizeof(int));					
		}
		if (strncmp(attr, ")", 1)!=0 || strncmp(attr, "(", 1)!=0){
			cur_pro -> pn = count;
			if (attr[0] == '('){
				attr[0] = ' ';
			}
			(cur_pro -> details)[num] = atoi(attr);
			num += 1;
		}	
		if (num == 4){
			num = 0;			
			queue[count] = malloc(sizeof(struct process));
			memcpy(queue[count], cur_pro, sizeof(struct process));
			count += 1;
			cur_pro = NULL;
		}	
		if (count == tp){
			break;
		}
	}
	printf("\n");

	//sort the original input
	struct process * cur = malloc(sizeof(struct process *));
	int i;
	int j;
	for (i=0; i<tp-1; i++){
		for (j=0; j<tp-1-i; j++){
			if ((queue[j]->details)[0] > (queue[j+1]->details)[0]){
				cur = queue[j];
				queue[j] = queue[j+1];
				queue[j+1] = cur;
			}
			else {
				if((queue[j]->details)[0] == (queue[j+1]->details)[0]){
					if ((queue[j]->details)[2] > (queue[j+1]->details)[2]){
						cur = queue[j];
						queue[j] = queue[j+1];
						queue[j+1] = cur;
					}
				}
			}
		}
	}

	printf("The sorted input is: %d ", tp);	
	num = 0;
	count = 0;
	while (count < tp){
		if (num == 0){
			printf("( ");
		}
		printf("%d ", (queue[count] -> details)[num]);
		num += 1;
		if (num == 4){
			printf(") ");
			count += 1;
			num = 0;
		}
	}
	printf("\n\n");

	for (i=0; i<tp; i++){
		queue[i] -> pn = i;
		queue[i] -> status = "unstarted";
		queue[i] -> cput = 0;
		queue[i] -> io = 0;
		queue[i] -> wt = 0;
		queue[i] -> ft = 0;
		queue[i] -> tl = (queue[i] -> details)[0];
		queue[i] -> rt = 0;
		queue[i] -> prevcpu = 0;
	}
	// if (algorithm == 1) fcfs(queue, tp, v);
	// if (algorithm == 2) rr(queue, tp, 2, v);
	// if (algorithm == 3) uni(queue, tp, v);
	// if (algorithm == 4) sjf(queue, tp, v);

	fcfs(queue, tp, v);
	rr(queue, tp, 2, v);
	uni(queue, tp, v);
	sjf(queue, tp, v);
	fclose(fp);
	return 0;
}