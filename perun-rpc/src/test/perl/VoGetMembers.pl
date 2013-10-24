use Perun::Agent;

$url = shift;
$voname = shift;

$agent = new Perun::Agent {url=>$url};
$mgr = getVosManager $agent;
#$vo = $mgr->getVoByShortName({shortName=>$voname});

@users = $mgr->getMembers(new Perun::Vo {manager=>$mgr,shortName=>$voname});

print "$_\n" for (@users);
