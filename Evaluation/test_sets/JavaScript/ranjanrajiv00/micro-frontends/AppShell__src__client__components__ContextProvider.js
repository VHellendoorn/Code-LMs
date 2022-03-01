import React from 'react';
import PropTypes from 'prop-types'

const ContextProvider = (WrappedComponent) => {
    class HOC extends React.Component {
        static childContextTypes = {
            insertCss: PropTypes.func,
        }
    
        getChildContext() {
            return { ...this.props.context }
        }
    
        render() {
            return <WrappedComponent {...this.props} />
        }
    }

    return HOC;
};
export default ContextProvider;