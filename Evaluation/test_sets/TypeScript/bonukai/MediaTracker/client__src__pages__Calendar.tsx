import React, { FunctionComponent, useMemo, useState } from 'react';
import { useQuery } from 'react-query';
import FullCalendar, { DatesSetArg } from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import listPlugin from '@fullcalendar/list';
import allLocales from '@fullcalendar/core/locales-all';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { MediaItemItemsResponse, MediaType, TvEpisode } from 'mediatracker-api';
import { mediaTrackerApi } from 'src/api/api';
import { formatEpisodeNumber } from 'src/utils';

export const CalendarPage: FunctionComponent = () => {
  const [datesSet, setDatesSet] = useState<DatesSetArg>();
  const { i18n } = useTranslation();

  const { data } = useQuery(
    ['calendar', datesSet?.startStr, datesSet?.endStr],
    () =>
      mediaTrackerApi.calendar.get({
        start: datesSet.startStr,
        end: datesSet.endStr,
      }),
    { enabled: datesSet !== undefined }
  );

  const episodeEvents = useMemo(
    () =>
      data?.episodes.map(
        (episode: TvEpisode & { tvShow: MediaItemItemsResponse }) => ({
          title: episode.tvShow.title,
          date: new Date(episode.releaseDate),
          allDay: true,
          backgroundColor: episodeColor(episode),
          borderColor: episodeColor(episode),
          id: episode.tvShowId.toString() + episode.id.toString(),
          extendedProps: {
            tvShowTitle: episode.tvShow.title,
            episodeNumber: formatEpisodeNumber(episode),
            episodeTitle: episode.title,
            seen: episode.seen,
            url: `/details/${episode.tvShowId}`,
          },
        })
      ),
    [data]
  );

  const mediaItemsEvents = useMemo(
    () =>
      data?.items.map((mediaItem) => ({
        title: mediaItem.title,
        date: new Date(mediaItem.releaseDate),
        allDay: true,
        backgroundColor: eventColor(mediaItem.mediaType),
        borderColor: eventColor(mediaItem.mediaType),
        extendedProps: {
          seen: mediaItem.seen,
          url: `/details/${mediaItem.id}`,
        },
      })),
    [data]
  );

  const events = useMemo(
    () => [...(episodeEvents || []), ...(mediaItemsEvents || [])],
    [episodeEvents, mediaItemsEvents]
  );

  return (
    <div className="p-2">
      <FullCalendar
        locales={allLocales}
        locale={i18n.language}
        plugins={[dayGridPlugin, listPlugin]}
        headerToolbar={{
          left: 'title',
          center: '',
          right: 'today prev,next dayGridMonth,listMonth',
        }}
        views={{
          listMonth: {
            displayEventTime: false,
          },
          dayGridMonth: {},
        }}
        initialView="dayGridMonth"
        dayMaxEvents={true}
        height="auto"
        datesSet={setDatesSet}
        events={events}
        eventContent={(arg) => (
          <Link to={arg.event.extendedProps.url}>
            <div className="flex">
              <span className="overflow-hidden text-ellipsis">
                {arg.event.title}
              </span>
              {arg.event.extendedProps.episodeNumber && (
                <span>&nbsp;{arg.event.extendedProps.episodeNumber}</span>
              )}
              {arg.event.extendedProps.seen && (
                <i className="pl-0.5 ml-auto flex material-icons">
                  check_circle_outline
                </i>
              )}
            </div>
          </Link>
        )}
      />
    </div>
  );
};

const eventColor = (mediaType: MediaType) => {
  switch (mediaType) {
    case 'book':
      return 'red';
    case 'tv':
      return 'blue';
    case 'movie':
      return 'orange';
    case 'video_game':
      return 'violet';
  }
};

const episodeColor = (episode: TvEpisode) => {
  return episode.episodeNumber === 1
    ? episode.seasonNumber === 1
      ? tvShowPremiereColor
      : seasonPremiereColor
    : null;
};

const seasonPremiereColor = 'orange';
const tvShowPremiereColor = 'green';
