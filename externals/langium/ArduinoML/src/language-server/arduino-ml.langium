grammar ArduinoMl

entry App:
	'app' name=ID 'initial' 'state' initial=[State:ID]
	'{'
		'bricks'
		bricks+=Brick
		(bricks+=Brick)* 
		
		'states'
		states+=State
		( states+=State)* 		
	'}';

Brick :
	(Actuator | Sensor);

Actuator:
	{infer Actuator}
	'Actuator' name=ID ':' outputPin=INT;

Sensor :
	{infer Sensor}
	'Sensor'  name=ID ':' inputPin=INT;
	
State :
	name=ID '{'
		actions+=Action
		(actions+=Action)* 
		expression=Expression
	'}';


Action :
	actuator=[Actuator:ID] '<=' value=Signal;

Expression:
    {infer Expression}
    condition=OrExpression transition=Transition;

OrExpression infers Expression:
    AndExpression ({infer OrExpression.left=current} operator=OR right=AndExpression)*;

AndExpression infers Expression:
    Condition ({infer AndExpression.left=current} operator=AND right=Condition)*;

Condition :
    sensor=[Sensor:ID] 'is' value=Signal;

Transition :
	sensor=[Sensor:ID] 'is' value=Signal '=>' next=[State:ID];

Signal:
    value=(HIGH | LOW);

Operator:
    value=(AND | OR);

terminal AND : 'and';
terminal OR : 'or';
terminal HIGH : 'HIGH';
terminal LOW : 'LOW';

hidden terminal WS: /\s+/;
terminal ID: /[_a-zA-Z][\w_]*/;
terminal INT returns number: /[0-9]+/;
terminal STRING: /"[^"]*"|'[^']*'/;

hidden terminal ML_COMMENT: /\/\*[\s\S]*?\*\//;
hidden terminal SL_COMMENT: /\/\/[^\n\r]*/;