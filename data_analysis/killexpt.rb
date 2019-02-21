#! /usr/bin/ruby

lines = `ps ux | grep ruby`.split "\n"
lines += `ps ux | grep java`.split "\n"
lines += `ps ux | grep -i matlab`.split "\n"


me = Process.pid.to_s
cmds = []
kill = []
lines.each {|l| cmds += [l.split]}
cmds.each {|c| kill += [c[1]] unless c.member? 'grep'}

kill.each {|k| `kill #{k}` unless k == me}
