import React, { useEffect, useRef, useState } from "react";
import { useSpeechContext } from "@speechly/react-client";
import { Calendar } from "./Calendar"
import { CalendarIcon } from "./CalendarIcon"
import { formatEntities } from "../utils"

export type VoiceDatePickerProps = {
  /**
   * The label displayed on the component. For speech use, the label should match the keywords in the phrase used to control the widget:
   * e.g. component with label "Passengers" should be configured to react to phrases like "3 passegers"
   */
  label: string

  /**
   * The current value. Specifying the value controls the components's state so it makes sense to provide an onChange handler.
   */
  value?: Date

  /**
   * Initially selected option. Has no effect if `value` is specified.
   */
  defaultValue?: Date

  /**
   * Specifies how this component reacts to intents in SpeechSegments.
   * Undefined value reacts to any intent.
   * String value (intent name) reacts to the single specified intent, e.g. "book"
   */
  changeOnIntent?: string

   /**
    * Specifies how this component reacts to entity types in SpeechSegments.
    * Undefined value reacts to any entity type.
    * Array of strings (entity types), one for each option, enables changing this widget's value to the option matching entity type.
    */
  changeOnEntityType: string

  /**
   * @private
   */
  focused?: boolean

  /**
   * @param value The selected date
   * Triggered upon GUI or voice manipulation of the widget.
   */
  onChange?: (value: Date) => void

  /**
   * @private
   */
  onVoiceBlur?: (el: HTMLInputElement) => void

  /**
   * @private
   */
  onVoiceFocus?: (el: HTMLInputElement) => void

  /**
   * @private
   */
  onFinal?: () => void
}

export const VoiceDatePicker = ({ label, value, defaultValue, changeOnIntent, changeOnEntityType, onChange, onFinal, onVoiceBlur, onVoiceFocus, focused = false }: VoiceDatePickerProps) => {

  const inputEl: React.RefObject<HTMLInputElement> = useRef(null)

  const [ _showCalendar, _setShowCalendar ] = useState(false)
  const [ _focused, _setFocused ] = useState(focused)
  const [ _date, _setDate ] = useState(defaultValue)
  const [ _value, _setValue ] = useState(defaultValue ? dateToString(defaultValue) : '')
  const [ lastGoodKnownValue, setLastGoodKnownValue ] = useState(defaultValue ? dateToString(defaultValue) : '')
  const { segment } = useSpeechContext()

  const _onChange = (newValue: string) => {
    _setValue(newValue)

    const newDate = stringToDate(newValue)
    if (newDate) {
      _setDate(newDate)
      if (onChange) {
        onChange(newDate)
      }
    }
  }

  function dateToString(date: Date): string {
    return `${(date.getUTCDate()).toString().padStart(2, '0')}/${(date.getUTCMonth() + 1).toString().padStart(2, '0')}/${date.getUTCFullYear()}`
  }

  function stringToDate(value: string): Date | null {
    const regex = /([\d]+)\D([\d]+)\D(\d\d\d\d)/
    const matches = value.match(regex)
    if (!matches) return null
    const [all, day, month, year] = matches;
    return new Date(Date.UTC(parseInt(year, 10), parseInt(month, 10) - 1, parseInt(day, 10),
        0, 0, 0, 0))
  }

  const _onVoiceFocus = () => {
    if (!_focused) {
      _setFocused(true)
      if (onVoiceFocus && inputEl.current) {
        onVoiceFocus(inputEl.current)
      }
    }
  }

  const _onVoiceBlur = () => {
    if (_focused) {
      _setFocused(false)
      if (onVoiceBlur && inputEl.current) {
        onVoiceBlur(inputEl.current)
      }
    }
  }

  useEffect(() => {
    if (value) {
      _setValue(dateToString(value))
      _setDate(value)
    }
  }, [value])

  useEffect(() => {
    if (segment) {
      let newValue = null

      // Define newValue if the segment contains input targeted to this component
      if (!changeOnIntent || segment.intent.intent === changeOnIntent) {
        let entities = formatEntities(segment.entities)
        if (entities[changeOnEntityType] !== undefined) {
          newValue = dateToString(new Date(Date.parse(entities[changeOnEntityType])))
        }
      }

      if (newValue !== null) {
        // Field is targeted
        if (!_focused) {
          setLastGoodKnownValue(value !== undefined ? dateToString(value) : _value)
          _onVoiceFocus()
        }
        _onChange(newValue)

        if (segment?.isFinal) {
          _onVoiceBlur()
          if (onFinal) {
            onFinal()
          }
        }
      } else {
        // Field is no longer targeted: tentative input may retarget to another component at any time
        if (_focused) {
          _onChange(lastGoodKnownValue)
          _onVoiceBlur()
        }
      }
    }
  }, [segment])

  const toggleCalendar = (e: React.FormEvent) => {
    e.preventDefault()
    _setShowCalendar(!_showCalendar)
  }

  const onDatePick = (pickedDate: Date) => {
    _setShowCalendar(!_showCalendar)
    _onChange(dateToString(pickedDate))
  }

  return (
    <div className={`widgetGroup inputText withCalendar ${_focused ? "voicefocus": ""}`}>
      <label>{ label }</label>
      <input
        ref={inputEl}
        type="text"
        name={changeOnEntityType}
        value={_value}
        onChange={(event: any) => { _onChange(event.target.value) }}
      />

      <button className="calendar-button" onClick={toggleCalendar}>
        <CalendarIcon />
      </button>

      { _showCalendar && <Calendar date={_date} onDatePick={onDatePick} /> }
    </div>
  );
}
