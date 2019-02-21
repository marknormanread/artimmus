import os

output = 'test_EAE'
param = 'single_run/test_EAE/baseline_Reg.xml"'

for run in range(100):
	command = "java -Xms600m -Xmx600m -classpath Treg_2D.jar:MASON.jar sim2d.experiment.SingleRun " +\
			  "-medians false -rawData true -startNum " + str(run) + " -seed " + str(run) +\
			  " -runs 1 -time 1560 -path test_EAE -param single_run/test_EAE/baseline_Reg.xml"
	print command
	os.system(command)