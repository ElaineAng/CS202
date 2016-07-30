Name: Ran (Elaine) Ang
OS Lab 4 Paging

How to compile:
	- go to the the directory that contains the "paging" directory, 
	- run command: `javac paging/*.java`

How to run:
	- run command: `java paging.Simulation {ARGS}`

	- where {ARGS} must contain the following 6 arguments:
		int M, the machine size in words.
		int P, the page size in words.
		int S, the size of a process
		int J, the job mix, which determines A, B, and C
		int N, the number of references for each process.
		string R, the replacement algorithm, LIFO, RANDOM, or LRU.

	  and {ARGS} takes an optional 7th argument "--v", that shows the verbose mode.

	- run example:
		`java paging.Simulation 800 40 400 4 5000 lru` for normal mode or
		`java paging.Simulation 800 40 400 4 5000 lru --v` for verbose mode


Thank you so much!