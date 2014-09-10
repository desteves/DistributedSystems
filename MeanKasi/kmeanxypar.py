'''
Created on Jul 30, 2014

@author: Linne
'''

import sys
import getopt
import math
import collections
import random

from mpi4py import MPI 

comm = MPI.COMM_WORLD # @UndefinedVariable  
rank = comm.Get_rank()
size = comm.Get_size()

isDNA = 0 #false

def usage():
    print '$> python MeanKasi.py <required args> [optional args]\n' + \
        '\t-d <0|1>\t\t0 = XY Dataset, 1 = DNA Dataset\n\t-k <#>\t\tNumber of clusters to generate\n\t-i <file>\tFilename for the input file \n\t-u [#]\t\tMaximum number of iterations\n'  

def handleArgs(args):
    global isDNA
    # set up return values
    k = -1
    inputFile = None
    u = 100
    try:
        optlist, args = getopt.getopt(args[1:], 'k:i:u:d:')
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
        elif key == '-d':
            isDNA = int(val)
    # check required arguments were right
    if k < 0 or u < 0 or inputFile is None or isDNA not in [0, 1]:
        usage()
        sys.exit()
    return (k, inputFile, u)

'''
Takes points and computes the Euclidean distance between them.
'''
def euclideanDistance(p, q):    
    #assert dim len(q)    
    sqsum = 0
    if not isDNA:
        for index, point in enumerate(p):
            #print 'point', point, 'q[index]', q[index]
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
        how_many = float(len(my_points))
        #print my_points
        for att, attribute in enumerate(centroid):
            totalsum = float(0)
            for my_point in my_points:
                totalsum = totalsum + float(my_point[att])
            
            new_val = 0
            try:
                new_val = totalsum/how_many
            except ZeroDivisionError:
                pass
            centroids[c][att] = str(new_val)
    return centroids

'''
% Difference between reassignment of records across clusters
'''
def calcDiff(clusters, oldlcusters):
    diffs = [c for c, cluster in enumerate(clusters) if cluster != oldlcusters[c] ]
    return float (float(len(diffs))/float(len(clusters)))

def xyClusters(points, cents, clusters):   
    cluster_info = cents[:]
    for c, info in enumerate(cluster_info):
        #grab all points that belong to this cluster
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        how_many = float(len(my_points))
        for att, attribute in enumerate(info):
                totalsum = float(0)
                for my_point in my_points:
                    totalsum = totalsum + float(my_point[att])
                cluster_info[c][att] = (totalsum, how_many)
    #print 'cluster_info', cluster_info, 'rank', rank #for testing
    return cluster_info
    
#####################################################
#####################################################
#####################################################
#MAIN
#####################################################
#####################################################
#####################################################

# start by reading the command line

k, inputFile, u = handleArgs(sys.argv)    
points = []    
threshold = float(0.05) # % of records change clusters

if rank == 0: #this is the master process
    # open file  # training data input 
    with open(inputFile+'-1', mode='r', buffering=-1) as input_file: # @UndefinedVariable    
        points = [line.rstrip().split(',') for line in input_file]
    ###########################################
    # step 1: place each centroids
    #pick random records from dataset in the first file
    centroids = [0] * k #points[0:k:] #[::-1]
    #randomize the inputs
    for index, c in enumerate(range(k)):
        centroids[index] = random.choice(points)
    ###########################################
    # step 2: Send message to all slave processing to start processing each file
    start = { 'message_type': "init", 'centroids': centroids }
    print '[Master] Sending start message'
    for i in range(0,size):
        comm.send(start, dest=i)
    ###########################################
    # step 3: iterate by reassinging records and re adjusting centroids until criteria is met    
    mu = 0
    diff = 1 
    unsorted_info = [None] * (size - 1)    
    cluster_info = [0] * k #first time all 0's
    old_cluster_info_size = [1] * k #first time all 1's
    while (mu < u):
        mu = mu + 1        
        messages = [None] * (size - 1)   
        reqs = [None] * (size - 1)   
        
        for m, msg in enumerate(messages):
            src = m + 1
            messages[m] = comm.recv(source=int(src))
        #untill all messages have been received
        #while (not any(mm for mm in messages)):
        #    pass
        print '[Master] Combining results & readjusting centroids' 

        ###########################################
        # step 4
        # compute the new centroid    
        #combine all results    & recalc centroids   
        for m, msg in enumerate(messages):
            xy = msg#.message
            if xy['message_type'] == 'cluster':
                unsorted_info[m] = xy['xy']
        diff_count = 0        
        for i in range(k):
            #get all the ith element from unsorted_info = info for kth cluster
            cluster_info = [cluster[i] for cluster in unsorted_info] 
            #how_many = len(cluster_info) #3
            x_total = sum( [float(tup[0][1]) for tup  in cluster_info ])
            x_sum   = sum( [float(tup[0][0]) for tup  in cluster_info ])
            y_total = sum( [float(tup[1][1]) for tup  in cluster_info ])
            y_sum   = sum( [float(tup[1][0]) for tup  in cluster_info ])
            how_many = x_total 
            #update centroids
            x_new = 0
            y_new = 0
            try: 
                x_new = float(x_sum)/float(x_total)
            except ZeroDivisionError:
                pass
            try: 
                y_new = float(y_sum)/float(y_total) 
            except ZeroDivisionError:
                pass                
            centroids[i] = [ x_new, y_new ]
            diff_count += diff_count + abs(how_many - old_cluster_info_size[i])
            old_cluster_info_size[i] = how_many #update the old with the new value
            
        #print old_cluster_info_size
        total_rows = sum(old_cluster_info_size)
        #print 'total_rows', total_rows
        try:
            diff = float(diff_count)/float(total_rows)
            print '[Master] Iteration difference', diff#, old_cluster_info_size
        except ZeroDivisionError:
            pass
        
        if diff > threshold: #if larger than threshold, reassing records
            cont = { 'message_type': 'continue', 'centroids': centroids }
            print '[Master] Sending continue message. '
            for i in range(0,size):
                comm.send(cont, dest=i)
        else:
            break
                
    print '[Master] Sending STOP message to all slaves'
    print '[Master] Final Centroids: '
    #print centroids
    for cent in centroids:
        print cent
    stop = { 'message_type': 'stop'}

    for i in range(0,size):
        comm.send(stop, dest=i)
        

    ###########################################    

elif rank != 0: # if it's a slave process, do work
    ########  receive messages from master and iterate until a stop message is received.
    clusters = []
    while 1:
        data = comm.recv(source=0)        
        msg = data['message_type']  
        print '[Slave', rank, '] Received: ', msg,' message.'
        if msg == 'stop':
            break        
        if msg == 'init':            
            print '[Slave', rank, '] Init '
            # open file
            with open(inputFile+'-'+str(rank), mode='r', buffering=-1) as input_file: # @UndefinedVariable    
                points = [line.rstrip().split(',') for line in input_file]    
            print '[Slave', rank, '] Opened file: ',inputFile+'-'+str(rank)          
            print '[Slave', rank, '] Starting Iteration Proc'
            clusters = [-1] * len(points)
                    
        centroids = data['centroids']
        #column indicating which record belongs to which centroid
        clusters  = reassignClusters(points, centroids)
        cluster_info = xyClusters(points, centroids, clusters)       
        xy = {'message_type': "cluster", 'xy': cluster_info}
        comm.send(xy, dest=0) #send clusters to mastahhh        
    print '[Slave', rank, '] Stopping process '
    
