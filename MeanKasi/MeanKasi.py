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


isDNA = 0 #false

def usage():
    print '$> python MeanKasi.py <required args> [optional args]\n' + \
        '\t-k <#>\t\tNumber of clusters to generate\n\t -o <outputfile>\t Filename for output\n\t  -i <file>\tFilename for the input file \n\t-u [#]\t\tMaximum number of iterations\n'

def handleArgs(args):
    
    # set up return values
    k = -1
    inputFile = None
    outputFile = None
    u = 100
    try:
        optlist, args = getopt.getopt(args[1:], 'k:i:u:o:')
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
        elif key == '-o':
            outputFile = val
        
    # check required arguments were right
    if k < 0 or u < 0 or inputFile is None or isDNA not in [0, 1]:
        usage()
        sys.exit()
    return (k, inputFile, u, outputFile)

'''
Takes points and computes the Euclidean distance between them.
'''
def euclideanDistance(p, q):    
    #assert dim len(q)    
    sqsum = 0
    if not isDNA:
        for index, point in enumerate(p):
            sqsum += math.pow ((float(point) - float(q[index])), 2)    
        return math.sqrt(sqsum)
    else:#if DNA is about how similar the base DNA strand is similar to current stand
        return sum(  [1 for pp, point in enumerate(p) if p[pp] == q[pp]  ]  )
    
'''
Compares all centroids to current record (points)
and returns the one with the shortest distance
'''
def minCentroid(centroids, points):
    minCent = -1
    minDist = sys.float_info.max #float("inf")
    for c, centroid in enumerate(centroids):
        dist = euclideanDistance(centroid, points)
        if minDist > dist:
            minDist = dist
            minCent = c
    return minCent

'''
Compares all centroids to all records
and returns the new cluster assignment
'''
def reassignClusters(points, centroids):   
    #column indicating which record belongs to which centroid
    clusters = [-1] * len(points)    
    for c, cluster in enumerate(clusters):
        clusters[c] = minCentroid(centroids, points[c])        
    return clusters

'''
Recomputes the centroid based on the points in its cluster
'''
def reassignCentroids(points, cents, clusters):   
    centroids = cents[:]
    for c, centroid in enumerate(centroids):
        #print 'old', ''.join(centroid) 
        #grab all points that belong to this cluster
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        #print 'my points total', my_points
        how_many = float(len(my_points))
        #print my_points
        if not isDNA:
            for att, attribute in enumerate(centroid):
                totalsum = float(0)
                for my_point in my_points:
                    totalsum = totalsum + float(my_point[att])
                centroids[c][att] = str(totalsum/how_many)
    return centroids

'''
% Difference between reassignment of records across clusters
'''
def calcDiff(clusters, oldlcusters):
    diffs = [c for c, cluster in enumerate(clusters) if cluster != oldlcusters[c] ]
    return float (float(len(diffs))/float(len(clusters)))

if __name__ == '__main__':
    # start by reading the command line
    k, inputFile, u, outputFile = handleArgs(sys.argv)
    points = []    
    # open file  # training data input 
    with open(inputFile, mode='r', buffering=-1) as input_file: # @UndefinedVariable    
        points = [line.rstrip().split(',') for line in input_file]    
    # step 1: place each centroids
    #pick random records from dataset
    centroids = points[0:k:] 
    lastIterationTime=time.time()
    #randomize the inputs
    for index, c in enumerate(range(k)):
        centroids[index] = random.choice(points)
    
    #column indicating which record belongs to which centroid
    clusters = [-1] * len(points)
    mu = 0
    diff = 1
    threshold = 0.05 # % of records change clusters
    #print 'isDNA', isDNA
    while (mu < u and diff > threshold):
        mu = mu + 1
        oldlcusters = clusters[:]
        clusters  = reassignClusters(points, centroids)
        centroids = reassignCentroids(points, centroids, clusters)
        newdiff = calcDiff(clusters, oldlcusters)
        #if newdiff < diff:
        #    break
        diff = newdiff
        print 'Iteration difference', diff
        print 'TIME-',time.time() - lastIterationTime
        lastIterationTime = time.time()

    file = open(outputFile,'w')

    for c, centroid in enumerate(centroids):
        file.write( '\n'+ str(c) + 'Cluster Centroid: ' + str(centroid)+'\n')
        file.write( '\tPoints: \n')
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        for p, point in enumerate(my_points):
            file.write('\t\t'+ str(p+1)+'\t'+str(point)+'\n')
