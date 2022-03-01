package vm

import (
	"unsafe"
	"reflect"
	"stone/environment"
	"stone/ast"
	"strconv"
)

/*
	register address [-1 ~ -7]
	heap address [0 ~ 4G]
 */

const (
	NUM_OF_REG     = 6
	SAVE_AREA_SIZE = NUM_OF_REG + 2
	TRUE           = 1
	FALSE          = 0
)

type VM struct {
	code    []int8
	stack   []interface{}
	strings []string
	heap    HeapMemory

	pc, fp, sp, ret int // pc program counter; fp frame pointer; sp stack pointer; ret return value
	registers       []interface{}
}

func NewVM(codeSize, stackSize, stringsSize int, hm HeapMemory) *VM {
	return &VM{code:   make([]int8, codeSize),
		stack:     make([]interface{}, stringsSize),
		strings:   make([]string, stringsSize),
		heap:      hm,
		registers: make([]interface{}, NUM_OF_REG)}
}

func (self *VM) GetReg(i int) interface{}        { return self.registers[i] }
func (self *VM) SetReg(i int, value interface{}) { self.registers[i] = value }
func (self *VM) Strings() []string               { return self.strings }
func (self *VM) Code() []int8                    { return self.code }
func (self *VM) Stack() []interface{}            { return self.stack }
func (self *VM) Heap() HeapMemory                { return self.heap }

func (self *VM) Run(entry int) {
	self.pc = entry
	self.fp = 0
	self.sp = 0
	self.ret = -1

	for self.pc >= 0 {
		self.mainLoop()
	}
}

func (self *VM) mainLoop() {
	switch self.code[self.pc] {
	case ICONST:
		self.registers[decodeRegister(self.code[self.pc + 5])] = readInt(self.code, self.pc + 1)
		self.pc += 6
	case BCONST:
		self.registers[decodeRegister(self.code[self.pc + 2])] = int(self.code[self.pc + 1])
		self.pc += 3
	case SCONST:
		self.registers[decodeRegister(self.code[self.pc + 3])] =
			self.strings[readShort(self.code, self.pc + 1)]
		self.pc += 4
	case MOVE:
		self.moveValue()
	case GMOVE:
		self.moveHeapValue()
	case IFZERO:
		value := self.registers[decodeRegister(self.code[self.pc + 1])]
		if i, ok := value.(int); ok && i == 0 {
			self.pc += readShort(self.code, self.pc + 2)
		} else {
			self.pc += 4
		}
	case GOTO:
		self.pc += readShort(self.code, self.pc + 1)
	case CALL:
		self.callFunction()
	case RETURN:
		self.pc = self.ret
	case SAVE:
		self.saveRegisters()
	case RESTORE:
		self.restoreRegisters()
	case NEG:
		reg := decodeRegister(self.code[self.pc + 1])
		v := self.registers[reg]
		if i, ok := v.(int); ok {
			self.registers[reg] = -i
		} else {
			panic("bad operand value")
		}
		self.pc += 2
	default:
		if self.code[self.pc] > LESS {
			panic("bad instruction")
		} else {
			self.computeNumber()
		}
	}
}

func (self *VM) moveValue() {
	src, dest := self.code[self.pc + 1], self.code[self.pc + 2]
	var value interface{}
	if isRegister(src) {
		value = self.registers[decodeRegister(src)]
	} else {
		value = self.stack[self.fp + decodeOffset(src)]
	}

	if isRegister(dest) {
		self.registers[decodeRegister(src)] = value
	} else {
		self.stack[self.fp + decodeOffset(src)] = value
	}

	self.pc += 3
}

func (self *VM) moveHeapValue() {
	rand := self.code[self.pc + 1]
	if (isRegister(rand)) {
		dest := readShort(self.code, self.pc + 1)
		self.heap.Write(dest, self.registers[decodeRegister(rand)])
	} else {
		src := readShort(self.code, self.pc + 1)
		self.registers[decodeRegister(self.code[self.pc + 3])] = self.heap.Read(src)
	}
	self.pc += 4
}

func (self *VM) callFunction() {
	value := self.registers[decodeRegister(self.code[self.pc + 1])]
	numOfArgs := int(self.code[self.pc + 2])

	if fn, ok := value.(*ast.VMFunction);
		ok && fn.Parameters().Size() == numOfArgs {
		self.ret = self.pc + 3
		self.pc = fn.Entry()
	} else if  fnc, ok := value.(*environment.NativeFunction);
		ok && fnc.NumParammeters() == numOfArgs {
		params := make([]reflect.Value, numOfArgs)
		for i := 0; i < numOfArgs; i++ {
			params[i] = reflect.ValueOf(self.stack[self.sp + i])
		}
		self.stack[self.sp] = fnc.Invoke(params)
		self.pc += 3
	} else {
		panic("bac function call")
	}
}

func (self *VM) saveRegisters() {
	size := decodeOffset(self.code[self.pc + 1])
	dest := size + self.sp

	for i := 0; i < NUM_OF_REG; i++ {
		self.stack[dest] = self.registers[i]
		dest++
	}
	self.stack[dest] = self.fp
	dest++
	self.fp = self.sp
	self.sp += size + SAVE_AREA_SIZE
	self.stack[dest] = self.ret
	self.pc += 2
}

func (self *VM) restoreRegisters() {
	dest := decodeOffset(self.code[self.pc + 1]) + self.fp
	for i := 0; i < NUM_OF_REG; i++ {
		self.registers[i] = self.stack[dest]
		dest++
	}
	self.sp = self.fp
	self.fp = self.stack[dest].(int)
	dest++
	self.ret = self.stack[dest].(int)
	self.pc += 2
}

func (self *VM) computeNumber() {
	leftIndex := decodeRegister(self.code[self.pc + 1])
	rightIndex := decodeRegister(self.code[self.pc + 2])
	left := self.registers[leftIndex]
	right := self.registers[rightIndex]
	leftKind := reflect.TypeOf(left).Kind()
	rightKind := reflect.TypeOf(right).Kind()

	if leftKind == reflect.Int && rightKind == reflect.Int {
		i1, i2 := left.(int), right.(int)
		var i3 int

		switch self.code[self.pc] {
		case ADD:i3 =  i1 + i2
		case SUB: i3 =  i1 - i2
		case MUL: i3 =  i1 * i2
		case DIV: i3 =  i1 / i2
		case REM: i3 =  i1 % i2
		case EQUAL: if i1 == i2 {
			i3 = TRUE
		} else {
			i3 = FALSE
		}
		case MORE: if i1 > i2 {
			i3 = TRUE
		} else {
			i3 = FALSE
		}
		case LESS: if i1 < i2 {
			i3 = TRUE
		} else {
			i3 = FALSE
		}
		default: panic("bad operator")
		}

		self.registers[leftIndex] = i3
	} else {
		if self.code[self.pc] == ADD {
			if leftKind == reflect.String && rightKind == reflect.String {
				self.registers[leftIndex] = left.(string) + right.(string)
			} else if leftKind == reflect.String && rightKind == reflect.Int {
				self.registers[leftIndex] = left.(string) + strconv.Itoa(right.(int))
			} else if leftKind == reflect.Int && rightKind == reflect.String {
				self.registers[leftIndex] = strconv.Itoa(left.(int)) + right.(string)
			} else {
				panic("bad operands for ADD")
			}
		} else if self.code[self.pc] == EQUAL {
			if left == right {
				self.registers[leftIndex] = TRUE
			} else {
				self.registers[leftIndex] = FALSE
			}
		} else {
			panic("bad instruction")
		}
	}
}

func readInt(b []int8, i int) int {
	x := uint32(b[i + 3]) | uint32(b[i + 2]) << 8 | uint32(b[i + 1]) << 16 | uint32(b[i]) << 24
	return int(*(*int32)(unsafe.Pointer(&x)))
}

func readShort(b []int8, i int) int {
	x := uint32(b[i + 1]) | uint32(b[i]) << 8
	return int(*(*int16)(unsafe.Pointer(&x)))
}
