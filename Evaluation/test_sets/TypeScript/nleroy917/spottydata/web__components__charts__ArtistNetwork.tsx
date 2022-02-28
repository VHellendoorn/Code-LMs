import { ResponsiveChord } from '@nivo/chord'
import { FC } from 'react'
import { chartColors } from './colors'

interface Props {
    collaborationMatrix: [][],
    artistNames: string[]
}

const ArtistNetwork: FC<Props> = (props) => {

    const { collaborationMatrix, artistNames } = props

    return (
        <ResponsiveChord
            matrix={collaborationMatrix}
            keys={artistNames}
            margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
            padAngle={0.02}
            innerRadiusRatio={0.90}
            innerRadiusOffset={0.02}
            arcOpacity={1}
            arcBorderWidth={2}
            arcBorderColor={{ from: 'color', modifiers: [ [ 'darker', 1.6 ] ] }}
            ribbonOpacity={0.65}
            ribbonBorderWidth={1}
            ribbonBorderColor={{ from: 'color', modifiers: [ [ 'darker', 1.6 ] ] }}
            enableLabel={false}
            label="id"
            labelOffset={12}
            labelRotation={-120}
            labelTextColor={{ from: 'color', modifiers: [ [ 'darker', 1 ] ] }}
            layers={['ribbons', 'arcs', 'labels', 'legends']}
            colors={chartColors}
            isInteractive={true}
            arcHoverOpacity={1}
            arcHoverOthersOpacity={0.25}
            ribbonHoverOpacity={0.75}
            ribbonHoverOthersOpacity={0.25}
            animate={true}
            motionStiffness={90}
            motionDamping={7}
        />
    )
}

export default ArtistNetwork