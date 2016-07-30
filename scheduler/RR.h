void rr(struct process **queue, int tp, int quant, bool v){ 
	bool finish = false; // whether fcfs finishes
	struct process * cpu; //current process in cpu
	struct process * cp = malloc(sizeof(struct process *)); //current process
	struct ready * rqh = malloc(sizeof(struct ready *)); //ready queue head

	int cycle = -1;
	int i;
	int maxfi = 0;
	int maxrt = 0; //maximum ready time among tp nodes in a certain cycle
	
	int idle = 0;
	int total_io = 0;

	cpu = NULL;
	randnum = fopen("random.txt","r");

	struct ready * node = malloc(sizeof(struct ready));
	node -> pro = NULL;
	node -> next = NULL;
	rqh = node;

	if (v) printf("This detailed printout gives the state and remaining burst for each process.\n\n");
	while (!finish){
		cycle += 1;
		if (v) printf("Before cycle %d: ", cycle);
		bool will = false;
		bool hasblock = false;
		for (i=0; i<tp; i++){
			cp = queue[i];

			if (strncmp(cp->status, "unstarted", 10) == 0){	
				if (v) printf("unstarted 0 ");	
				//print_queue(rqh);
				if (cp -> tl > 0){	
					cp -> tl -= 1; //still unstarted
				}
				else{ // cp -> tl == 0
					cp -> tl = randomOS((cp -> details)[1]); //next cpu burst time
					if (cp -> cput + cp -> tl > (cp -> details)[2]){
						cp -> tl = (cp -> details)[2] - (cp -> cput);
					}// if current cpu burst time will lead to a terminate status
					cp -> prevcpu = cp -> tl; //store it for calculating io burst time	
					
					cp -> status = "ready";
					add_to_queue(rqh, cp);											
				}					
			}
			else{
				if (strncmp(cp -> status, "running", 8) == 0){
					if (cp -> tl > 0){
						if (cpu != NULL && (cp != cpu || cp -> rt >= quant)){
							if (cp -> rt >= quant){
								cpu = NULL;
								cp -> rt = 0;
							}
							if (v) printf("ready 0 ");
							//print_queue(rqh);
							cp -> status = "ready";
							add_to_queue(rqh, cp);
						}

						else{
							cpu = cp;
							if (v) printf("running %d ", cp -> tl);
							//print_queue(rqh);

							will = false;
							cp -> tl -= 1; 
							cp -> rt += 1;
							if (cp -> tl == 0 || cp -> rt >= quant) {
								will = true;
							}
							if (cp -> rt >= quant && cp -> tl != 0){
								cp -> rt = 0;
								cp -> status = "ready";
								add_to_queue(rqh, cp);
							}
							else{
								cp -> status = "running";
							}
															
						}
					}

					else{	// cp -> tl == 0
						if (cpu == cp){
							cpu = NULL;
						}
						cp -> cput += (cp -> prevcpu); //add total cpu time
						if (cp->cput == (cp -> details)[2]){
							cp -> ft = cycle;
							cp -> status = "terminated";
							if (v) printf("terminated 0 ");
						}
						else{
							cp -> rt = 0;
							cp -> status = "blocked";
							cp -> tl = (cp -> prevcpu) * (cp -> details)[3]; // get the io burst time						
							if (v) printf("blocked %d ", cp -> tl);
							hasblock = true;
							cp -> tl -= 1;
							if (cp -> tl == 0){
								cp -> io += (cp->prevcpu * (cp -> details)[3]); //add total io time
								cp -> tl = randomOS((cp -> details)[1]); 				
								if (cp -> cput + cp -> tl > (cp -> details)[2]){
									cp -> tl = (cp -> details)[2] - (cp -> cput);
								}
								cp -> prevcpu = cp -> tl;
								cp -> status = "ready";
								add_to_queue(rqh, cp);
							}
						}						
					}			
				}
				else{
					if (strncmp(cp->status, "blocked", 8) == 0){				
						if (cp -> tl > 0){
							if (v) printf("blocked %d ", cp->tl);
							hasblock = true;
							//print_queue(rqh);
							cp -> tl -= 1;
							if (cp -> tl == 0){
								cp -> io += (cp->prevcpu * (cp -> details)[3]); //add total io time
								update_time_left(cp);
								cp -> status = "ready";
								add_to_queue(rqh, cp);
							}
						}
						else{
							cp -> io += (cp->prevcpu * (cp -> details)[3]); //add total io time
							update_time_left(cp);
							cp -> status = "ready";
							add_to_queue(rqh, cp);
						}
					}
					else{
						if (strncmp(cp->status, "ready", 6) == 0){												
							if (cpu == NULL){
								if(rqh -> next != NULL){

									if (cp == rqh -> next -> pro){
										cp -> status = "running";
										cpu = cp;
										will = false;
										if (cp -> tl == 0){
											update_time_left(cp);
										}
										if (v) printf("running %d ", cp -> tl);

										cp -> tl -= 1;
										cp -> rt += 1;
										if (cp -> tl == 0 || cp -> rt >= quant){
											will = true;
										}

										rqh -> next = rqh -> next -> next;	
										//print_queue(rqh);
									}

									else{
										cp -> wt += 1;
										if (v) printf("ready 0 ");
										//print_queue(rqh);
										cp -> status = "ready";
										add_to_queue(rqh, cp);
									}
								}
								else{
									printf("You really should not reach this line.\n");
								}
							}
							else{
								cp -> wt += 1;
								if (v) printf("ready 0 ");
								cp -> status = "ready";
								add_to_queue(rqh, cp);
								//print_queue(rqh);
							}
						}
						else if (strncmp(cp->status, "terminated", 11) == 0){
							if (v) printf("terminated 0 ");
						}
					}
				}
			}
		}

		bool io_using = false;
		for (i=0; i<tp; i++){
			if (strncmp(queue[i] -> status, "running", 8) == 0){
				cpu = queue[i];
			}
		}

		if (cpu == NULL) idle += 1;
		if (hasblock == true) total_io += 1;
		// cpu == NULL? printf(" NULL "): printf(" not NULL ");
		// will == true? printf(" will "): printf(" will not");
		if (will == true) cpu = NULL;

		for (i=0; i<tp; i++){
			if (strncmp(queue[i]->status, "terminated", 11) != 0){
				finish = false;
				break;
			}
			else{
				finish = true;
			}			
		}

		if (v) printf("\n");
	}
	printf("The scheduling algorithm used was Round Robbin.\n\n");
	
	float total_idle= idle - 2;
	float total_cycle = cycle - 1;
	float total_turnaround = 0;
	int tunaround = 0;
	float total_waiting = 0;
	int waiting = 0;
	for (i=0; i<tp; i++){
		printf("Process %d:\n", queue[i] -> pn);
		printf("(A,B,C,M) = ");
		printf("(%d, %d, %d, %d)\n", queue[i]->details[0],queue[i]->details[1],queue[i]->details[2],queue[i]->details[3]);
		printf("Finishing time: %d\n",queue[i]->ft-1);
		if (queue[i]->ft-1 > maxfi){
			maxfi = queue[i]->ft-1;
		}
		tunaround = queue[i]->ft-1 - queue[i]->details[0];
		total_turnaround += tunaround;
		waiting = queue[i]->wt;
		total_waiting += waiting;
		printf("Turnaround time: %d\n", tunaround);
		printf("I/O time: %d\n", queue[i]->io);
		printf("Waiting time: %d\n\n", waiting);
	}

	printf("Summary Data: \n");
	printf("Finishing time: %d\n", maxfi);	
	printf("CPU Utilization: %f\n", (total_cycle-total_idle)/total_cycle);	
	printf("I/O Utilization: %f\n", total_io/total_cycle);
	printf("Throughput: %f processes per hundred cycles\n", tp/total_cycle*100);
	printf("Average turnaround time: %f\n", total_turnaround/tp);
	printf("Average waiting time: %f\n", total_waiting/tp);
	fclose(randnum);
}