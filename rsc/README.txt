1. How to generate directed graphs using nauty  

One of nauty's tools is called directg, which takes undirected graphs as input and generates all possible directed versions of such graphs. So, you could generate all the simple graphs you want with geng and pass them to directg. Example:

../nauty27rc1/geng 5 -c -l | ../nauty27rc1/directg -o > connected_n5_directed.g6

In this command, I am using geng to create all connected (-c option) undirected graphs on 5 nodes, while also adding the -l option so that these graphs are canonically labeled (to guarantee uniqueness). Then, I'm piping the output from geng into directg to compute the directed graphs. The -o option in directg means each edge must be directed in one direction or the other, but not both at the same time, i.e. no "bidirected" edges. Also note that in the command above, I am printing the final output to a file named connected_n5_directed.g6. All generated graphs are in g6 format, which is a compact representation of an adjacency matrix.



2. How to generate a file showing the orbits of such graphs

In order to compute orbits, I used a C library published on this blog (https://computationalcombinatorics.wordpress.com/2012/10/05/computing-orbits-with-nauty/). However, please use the attached version (orca_orbits), which I have updated and compiled so that it takes a list of graphs from a g6 file, and prints the output like this:

./orca_orbits connected_n5_directed.g6 > connected_n5_directed_orbits.txt

Each printed line contains one graph in g6 format, followed by space-separated orbits. Each orbit in the printed line is then a set of comma-separated vertices (every vertex has a numeric label from 0 to n-1). 



3. Convert to adjacency matrix

../nauty27rc1/showg -a connected_n5_directed.g6 > connected_n5_directed_adjacency_matrix.txt



4. How to convert nauty's output from g6 format to our format 

Run GetNautyGraphs.