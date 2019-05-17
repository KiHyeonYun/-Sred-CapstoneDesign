import tensorflow as tf
import numpy as np
from tensorflow.contrib import rnn
import pprint
tf.set_random_seed(777)  # reproducibility
pp = pprint.PrettyPrinter(indent=4)
sess = tf.InteractiveSession()

# RNN model
# rnn_cell - rnn_cell.BasicRNNCell(rnn_size)
# outputs, _states = tf.nn.dynamic_rnn(rnn_cell,
#                                   X,
#                                   initial_state=initial_state,
#                                   dtype=tf.float32)

hidden_size = 5
input_dim = 5
batch_size = 1
sequence_length = 6

idx2char = ['h','i','e','l','o']
x_data = [[0,1,0,2,3,3]]
x_one_hot = [[[1,0,0,0,0],
             [0,1,0,0,0],
             [1,0,0,0,0],
             [0,0,1,0,0],
             [0,0,0,1,0],
             [0,0,0,1,0]]]

y_data = [[1,0,2,3,3,4]]

X = tf.placeholder(
    tf.float32, [None, sequence_length, input_dim]) # X one-hot
Y = tf.placeholder(
    tf.int32, [None, sequence_length]) # Y label

cell = tf.contrib.rnn.BasicLSTMCell(num_units=hidden_size, state_is_tuple=True)
initial_state = cell.zero_state(batch_size, tf.float32)
outputs, _states = tf.nn.dynamic_rnn(
    cell, X, initial_state=initial_state, dtype=tf.float32)

weights = tf.ones([batch_size, sequence_length])
sequence_loss = tf.contrib.seq2seq.sequence_loss(
    logits=outputs, targets=Y, weights=weights)
loss = tf.reduce_mean(sequence_loss)
train = tf.train.AdamOptimizer(learning_rate=0.1).minimize(loss)

prediction = tf.argmax(outputs, axis=2)
with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())
    for i in range(2000):
        l, _ = sess.run([loss, train], feed_dict={X: x_one_hot, Y: y_data})
        result = sess.run(prediction, feed_dict={X: x_one_hot})
        print(i, "loss:", l, "prediction:", result, "true Y:", y_data)
        
        # print char using dic
        result_str = [idx2char[c] for c in np.squeeze(result)]
        print("\t\tPrediction str : ", ''.join(result_str))