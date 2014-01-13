1. Goal: group/cluster keywords. Keywords that share a high degree of meaning or intent are gathered together in a cluster.

2. Input/Output:
- input: file of keywords
- output: groups of keywords
eg. Total 10 clusters (cluster size =  30, SumOfSquaredErrors = 1929.32): 
    cluster 0 :     
    keyword1 
    keyword2
    keyword3
    ...
note: For most of the clusters the number of keywords in the cluster from origial clustering algorithm is much bigger than 30. Since it was asked in the requirement to group approximately 30 keywords in one cluster, I select 30 keywords that are nearest to centroid of the cluster.

3. preprocess
- ignore stopwords: Stopwords list from Rainbow
- ignore non-dictionary word: dictionary word from wordnet

4. core algorithm
- use svd to make sparse matrix to dense matrix which contains more information for clustering
- kmeans/kmedoid: javaml

5. additional thoughts (need more time)
- can further refine the results by considering the relative order of terms appear in the keyword
- some other variables to determine the relevancy
- several parameters can be tweaked and experiments can be done to further optimize the result

6. Run:
java -jar MyCluster.jar inputFileName clusterNum clusterSize
eg. java -jar MyCluster.jar "IV6-RAWkeywords.txt" 10 30



