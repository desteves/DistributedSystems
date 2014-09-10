'''
Created on Jul 30, 2014

@author: Linne
'''

import sys
import getopt
import math
import random
import time
import collections

from mpi4py import MPI 

comm = MPI.COMM_WORLD # @UndefinedVariable  
rank = comm.Get_rank()
size = comm.Get_size()


def usage():
    print '$> python MeanKasi.py <required args> [optional args]\n' + \
        '\t-k <#>\t\tNumber of clusters to generate\n\t-i <file>\tFilename for the input file \n\t-u [#]\t\tMaximum number of iterations\n'  

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
    if k < 0 or u < 0 or inputFile is None:
        usage()
        sys.exit()
    return (k, inputFile, u)

'''
Takes points and computes the Euclidean distance between them.
'''
def euclideanDistance(p, q):  
    #how alike these 2 species are  
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

'''
% Difference between reassignment of records across clusters
'''
def calcDiff(clusters, oldlcusters):
    diffs = [c for c, cluster in enumerate(clusters) if cluster != oldlcusters[c] ]
    return float (float(len(diffs))/float(len(clusters)))


'''
Returns info about the processe's records and where they belong
'''
def DNAClusters(points, cents, clusters):   
    cluster_info = [None] * len(cents)
    for c in range(0, len(cents)):
        #grab all points that belong to this cluster
        my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
        #print 'my_points', len(my_points)#, my_points[0] #['AGGTTGCACGGGTCTAAAGT']
        if my_points:            
            dna_strand = len(my_points[0][0]) #20 character sequence
            #new_dna = ""
            my_points_info = [None] * dna_strand
            for att in range(0,dna_strand):
                att_point = ""
                for p, my_point in enumerate(my_points):
                    att_point += str(my_point[0][att])    
                #print att,'att_point', att_point
                if att_point:     
                    my_points_info[att] = collections.Counter(att_point).most_common()  
            cluster_info[c] = (len(my_points), my_points_info )#tuple 
    #print 'cluster_info[0]', cluster_info[0]#, 'rank', rank #for testing
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
threshold = float(0.001) # % of records change clusters


if rank == 0: #this is the master process
    #parse the input file
    # open file  # training data input 
    with open(inputFile, mode='r', buffering=-1) as input_file: # @UndefinedVariable    
        points = [line.rstrip().split(',') for line in input_file]        
    splitLen = len(points)/(size-1) + 1# 20 lines per file
    orgInput = open(inputFile, 'r').read().split('\n')   
    
    ###Edited FROM the following link
    ###https://stackoverflow.com/questions/546508/how-can-i-split-a-file-in-python
    at = 1
    for lines in range(0, len(orgInput), splitLen):
        # First, get the list slice
        outputData = orgInput[lines:lines+splitLen]    
        # Now open the output file, join the new slice with newlines
        # and write it out. Then close the file.
        output = open(inputFile +'-'+ str(at), 'w')
        output.write('\n'.join(outputData))
        output.close()    
        # Increment the counter
        at += 1
    ###END FROM
    
    
    
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
    lastIterationTime=time.time()
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
            dna = msg#.message
            if dna['message_type'] == 'cluster':
                unsorted_info[m] = dna['dna']
        default_freq =  collections.Counter({'A': 0, 'C': 0, 'G': 0, 'T': 0})
        
        diff_count = 0              
        #print 'unsorted_info\n', unsorted_info
        for i in range(0, k):            
            #get all the ith element from unsorted_info = info for kth cluster as a list 
            #with p elements, where p is the number of slaves
            ''' kth_cluster_info = []
            for cc, clusters in enumerate(unsorted_info):
                if clusters:
                    print 'clusters---', i,  k
                    try:
                        kth_cluster_info.append(clusters[i])
                    except Exception:
                        pass
            '''        
            
            kth_cluster_info = [clusters[i] for clusters in unsorted_info]   
            kth_cluster_size = 0
            dna_strand_len = len(points[0][0])
            kth_cluster_dna = {}
            #print 'dna_strand_len', dna_strand_len
            for char in range(0, dna_strand_len):
                kth_cluster_dna[char] = default_freq
            
            #update centroids
            #need to gather all the frequencies of all letters to determine the highest
            #print 'kth_cluster_dna', kth_cluster_dna
            for kth  in kth_cluster_info:
                if kth and kth[0]:
                    #print 'k', kth[0]
                    kth_cluster_size = kth_cluster_size + kth[0]
                    
                    if kth[1]: #not none, grab the procs dna strand freq
                        #kth_cluster_att_freq = [None] * len(kth[1])
                        dna_strand_len = len(kth[1]) #ie 20
                        
                        for a, att_freq in enumerate(kth[1]):                                                    
                            kth_cluster_dna[a] = collections.Counter(dict(att_freq)) + collections.Counter(kth_cluster_dna[a])
                            #print 'updating freq: ', kth_cluster_dna[a]
                            #tup = att_freq[0]
            #print '\n cluster',i, 'size', kth_cluster_freq, ' kth_cluster_dna', kth_cluster_dna            
            kth_updated_centroid = ""            
            for seq, dk in kth_cluster_dna.iteritems():
                kth_updated_centroid += str(dk.most_common(1)[0][0])     
            #print 'cluster',i, 'new centroid', kth_updated_centroid        
            centroids[i] = [kth_updated_centroid]
            #################
            #Update the difference from last run
            diff_count += diff_count + abs(kth_cluster_size - old_cluster_info_size[i])
            old_cluster_info_size[i] = kth_cluster_size #update the old with the new value
            #print i, 'cluster has', how_many, 'dna strands'    
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
            
            print '[Master] TIME-',time.time() - lastIterationTime
            lastIterationTime = time.time()
        else:
            break
                
    print '[Master] Sending STOP message to all slaves'
    print '[Master] Final Centroids: '
    for cent in centroids:
        print cent
    '''     
    for c, centroid in enumerate(centroids):
    print '\n', c, 'Cluster Centroid:', centroid[0]
    print '\tPoints:'
    my_points = [point for p, point in enumerate(points) if clusters[p] == c ]
    for p, point in enumerate(my_points):
        print '\t\t', p+1,point[0] 
    '''
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
        cluster_info = DNAClusters(points, centroids, clusters)       
        dna = {'message_type': "cluster", 'dna': cluster_info}
        comm.send(dna, dest=0) #send clusters to mastahhh        
    print '[Slave', rank, '] Stopping process '
    
