'''
Created on Jul 30, 2014

@author: Linne
'''

import sys
import getopt
import math
import collections
import random
import time
import operator

def usage():
    print '$> python MeanKasi.py <required args> [optional args]\n' + \
        '\t\t-k Number of clusters to generate\n\t-i <file>\tFilename for the input file \n\t-u [#]\t\tMaximum number of iterations\n'  

def handleArgs(args):
    # set up return values
    k = -1
    inputFile = None
    u = 100
    try:
        optlist, args = getopt.getopt(args[1:], 'k:i:u:')
    except getopt.GetoptError, err:
        print str(err)
        usage()
        sys.exit(2)
    for key, val in optlist:
        # first, the required arguments
        if   key == '-k':
            k = int(val)
        elif key == '-i':
            inputFile = val
        # now, the optional argument
        elif key == '-u':
            u = int(val)
    # check required arguments were right
    if k < 0 or u < 0 or inputFile is None :
        usage()
        sys.exit()
    return (k, inputFile, u)

'''
Takes points and computes the Euclidean distance between them.
'''
def euclideanDistance(p, q):    
    #print 'p', p, '\nq', q
    return sum(  [1 for pp, point in enumerate(p) if p[pp] == q[pp]  ]  )
    
'''
Compares all centroids to current record (points)
and returns the one with the shortest distance
'''
def minCentroid(centroids, dna):
    #the max of how the points is as compared to each centroid
    minCent = -1 #kluster thats closest to this specie (points)
    minDist = -1 #float("inf")
    for c, centroid in enumerate(centroids):
        dist = euclideanDistance(centroid[0], dna[0])
        if minDist < dist: #if current kluster isnt as alike as new distance
            minDist = dist
            minCent = c
    #print 'cluster for dna', dna[0], 'is centroid', centroids[minCent][0]
    return minCent

'''
Compares all centroids to all records
and returns the new cluster assignment
'''
def reassignClusters(dna_strands, centroids):   
    #column indicating which record belongs to which centroid
    clusters = [-1] * len(dna_strands)
    #print 'len clus', len(clusters)        
    for c, cluster in enumerate(clusters):
        clusters[c] = minCentroid(centroids, dna_strands[c])        
    return clusters

'''
Recomputes the centroid based on the points in its cluster
'''
def reassignCentroids(points, cents, clusters):   
    centroids = cents[:]
    for c, centroid in enumerate(centroids):
        #grab all points that belong to this cluster
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        if my_points:            
            sq_len = len(my_points[0][0])
            new_dna = ""
            for att in range(0,sq_len):
                att_point = ""
                for p, my_point in enumerate(my_points):
                    att_point += str(my_point[0][att])                
                
                if att_point:     
                    top_tupe = collections.Counter(att_point).most_common(1)[0]  
                    #freq = float(top_tupe[1])/float(len(att_point)) 
                    #if freq > 0.8:
                    new_dna += str(top_tupe[0])
                    #else:
                    #    new_dna += centroids[c][0][att]
        centroids[c] = [new_dna]       
    return centroids

def calcDiff(clusters, oldlcusters):
    diffs = [c for c, cluster in enumerate(clusters) if cluster != oldlcusters[c] ]
    return float (float(len(diffs))/float(len(clusters)))

if __name__ == '__main__':
    # start by reading the command line
    k, inputFile, u = handleArgs(sys.argv)    
    points = []    
    # open file  # training data input 
    with open(inputFile, mode='r', buffering=-1) as input_file: # @UndefinedVariable    
        points = [line.rstrip().split(',') for line in input_file]    
    # step 1: place each centroids
    #pick random records from dataset
    centroids = points[0:k:] #[::-1]

    lastIterationTime=time.time()

    #randomize the inputs
    for index, c in enumerate(range(k)):
        centroids[index] = random.choice(points)

    #column indicating which record belongs to which centroid
    clusters = [-1] * len(points)
    mu = 0
    diff = 1
    threshold = 0.001 # % of records change clusters
    while (mu < u and diff > threshold):
        mu = mu + 1
        oldlcusters = clusters[:]
        clusters  = reassignClusters(points, centroids)
        centroids = reassignCentroids(points, centroids, clusters)
        
        newdiff = calcDiff(clusters, oldlcusters)
        diff = newdiff
        print 'Iteration difference', diff
        print 'TIME-',time.time() - lastIterationTime
        lastIterationTime = time.time()
    for c, centroid in enumerate(centroids):
        print '\n', c, 'Cluster Centroid:', centroid[0]
        print '\tPoints:'
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        for p, point in enumerate(my_points):
            print '\t\t', p+1,point[0]
